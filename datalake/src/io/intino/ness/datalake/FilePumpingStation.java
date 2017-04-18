package io.intino.ness.datalake;

import io.intino.ness.datalake.NessDataLake.Joint;
import io.intino.ness.inl.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import static io.intino.ness.datalake.FileChannel.Format.*;

import static java.nio.file.StandardOpenOption.*;
import static java.util.Arrays.stream;

@SuppressWarnings("unchecked")
public class FilePumpingStation implements NessPumpingStation {

    private final File folder;
    private final Map<String, List<Pipe>> pipes = new HashMap<>();
    private Map<String, Joint> joints = new HashMap<>();
    private static final String Feed = "feed";

    public FilePumpingStation(String folder) {
        this(new File(folder));
    }

    public FilePumpingStation(File folder) {
        this.folder = folder;
    }

    @Override
    public void create(String channel) {
        assertNotExists(get(channel)).create();
    }

    @Override
    public void remove(String channel) {
        assertExists(get(channel)).remove();
    }

    @Override
    public void rename(String channel, String newName) {
        assertExists(get(channel)).rename(newName);
    }

    @Override
    public boolean exists(String channel) {
        return get(channel).exists();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private File[] setOf(File file) {
        File zipFile = zipOf(file);
        return zipFile.exists() ? new File[] {zipFile, file} : new File[] {file};
    }

    private static File zipOf(File file) {
        return new File(file.getAbsolutePath().replace(".inl",".zip"));
    }

    private void write(String channel, Message message)  {
        try {
            File file = get(channel).fileOf(message,inl);
            write(file, message);
            pipes.get(channel).forEach(p->p.send(message));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void write(File file, Object message) throws IOException {
        Files.write(file.toPath(), bytesOf(message), APPEND, CREATE);
    }

    @Override
    public Channeling pipe() {
        return pipe(Feed);
    }

    @Override
    public Channeling pipe(String source) {
        return new Channeling() {

            private List<MessageMapper> mappers = new ArrayList<>();

            @Override @SuppressWarnings("unchecked")
            public Channeling map(String function) throws Exception {
                return map(mapperClassOf(function));
            }

            @Override @SuppressWarnings("unchecked")
            public Channeling map(String function, String... sources) throws Exception {
                return map(compile(function, sources).as(MessageMapper.class));
            }

            @Override
            public Channeling map(Class<? extends MessageMapper> mapperClass) throws Exception {
                return map(mapperClass.newInstance());
            }

            @Override
            public Channeling map(MessageMapper mapper) {
                this.mappers.add(mapper);
                return this;
            }

            @Override
            public Channeling filter(String function) throws Exception {
                return filter(filterClassOf(function));
            }

            @Override
            public Channeling filter(String function, String... sources) throws Exception {
                return filter(compile(function, sources).as(MessageFilter.class));
            }

            @Override
            public Channeling filter(Class<? extends MessageFilter> filterClass) throws Exception {
                return filter(filterClass.newInstance());
            }

            @Override
            public Channeling filter(MessageFilter filter) throws Exception {
                return map(input -> filter.filter(input) ? input : null);
            }

            @Override
            public Pipe to(String target) {
                if (!isFeed(source) && !isEmpty(source) && isEmpty(target))
                    put(source, storePipeOf(target));
                return put(source, feedPipeOf(target));
            }

            private boolean isFeed(String source) {
                return source.equals(Feed);
            }

            private boolean isEmpty(String channel) {
                return assertExists(get(channel)).isEmpty();
            }

            @Override
            public Pipe to(Pipe pipe) {
                return put(source, wrapOf(pipe));
            }

            @Override
            public Channeling join(Joint joint) {
                joints.put(source, joint);
                return this;
            }

            private Pipe feedPipeOf(String channel) {
                return message -> write(channel, cast(message));
            }

            private Pipe storePipeOf(String channel) {
                FileChannel store = get(channel);
                return new SingleUsePipe() {
                    @Override
                    public void send(Message message) {
                        store.write(cast(message));
                    }

                    @Override
                    public void flush() {
                        store.close();
                    }
                };
            }

            private Pipe wrapOf(Pipe pipe) {
                return pipe instanceof SingleUsePipe ?
                        wrapOf((SingleUsePipe) pipe):
                        message -> pipe.send(cast(message));
            }

            private Pipe wrapOf(SingleUsePipe pipe) {
                return new SingleUsePipe() {
                    @Override
                    public void send(Message message) {
                        pipe.send(cast(message));
                    }

                    @Override
                    public void flush() {
                        pipe.flush();
                    }
                };
            }
            private Message cast(Message message) {
                for (MessageMapper function : mappers) {
                    if (message == null) return null;
                    message = function.map(message);
                }
                return message;
            }

        };
    }

    @Override
    public boolean close(Pipe pipe) {
        for (List<Pipe> pipes : this.pipes.values())
            if (pipes.contains(pipe)) return pipes.remove(pipe);
        return false;
    }

    @Override
    public Task pump(String channel) throws Exception {
        return pumpTaskFor(assertExists(get(channel)));
    }

    @Override
    public Task seal(String channel) {
        return sealTaskFor(assertExists(get(channel)));
    }

    private Task pumpTaskFor(FileChannel channel) throws Exception {

        return new Task() {
            NessFaucet faucet;
            List<Pipe> pipes;

            public boolean init() {
                faucet = new NessFaucet(channel);
                pipes = FilePumpingStation.this.pipes.get(channel.name());
                return pipes != null && pipes.size() != 0;
            }

            public boolean step() {
                try {
                    Message message = faucet.next();
                    if (message == null) return false;
                    pipes.forEach(p -> p.send(message));
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void onTerminate()  {
                pipes.forEach(this::flush);
                pipes.removeIf(this::isSingleUse);
            }

            private boolean isSingleUse(Pipe pipe) {
                return pipe instanceof SingleUsePipe;
            }

            private void flush(Pipe pipe) {
                if (isSingleUse(pipe)) ((SingleUsePipe) pipe).flush();
            }

        };
    }

    private Task sealTaskFor(final FileChannel channel) {
        return new Task() {
            Iterator<File> iterator;
            FileChannel fileChannel = assertExists(channel);

            @Override
            protected boolean init() {
                File[] files = fileChannel.files(inl);
                this.iterator = stream(files).iterator();
                return files.length > 0;
            }

            @Override
            protected boolean step()  {
                if (!iterator.hasNext()) return false;
                try {
                    seal(iterator.next());
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            private void seal(File file) throws IOException {
                MessageInputStream is = FileMessageInputStream.of(setOf(file));
                FileMessageOutputStream os = FileMessageOutputStream.of(zipOf(file));
                while (true) {
                    Message message = is.next();
                    if (message == null) break;
                    os.write(message);
                }
                os.close();
                is.close();
                file.delete();
            }

        };
    }

    @SuppressWarnings("unchecked")
    private Class<MessageFilter> filterClassOf(String name) throws ClassNotFoundException {
        return (Class<MessageFilter>) Class.forName(name);
    }

    @SuppressWarnings("unchecked")
    private Class<MessageMapper> mapperClassOf(String name) throws ClassNotFoundException {
        return (Class<MessageMapper>) Class.forName(name);
    }

    private NessCompiler.Result compile(String function, String... sources) {
        return NessCompiler.compile(sources)
                .with("-target", "1.8")
                .load(function);
    }

    private Pipe put(String channel, Pipe pipe) {
        if (!pipes.containsKey(channel)) pipes.put(channel, new ArrayList<>());
        pipes.get(channel).add(pipe);
        return pipe;
    }


    private FileChannel get(String channel) {
        return new FileChannel(new File(this.folder, channel), joints.get(channel));
    }

    private static byte[] bytesOf(Object message) {
        if (message == null) return new byte[0];
        if (message instanceof Message) message = message.toString();
        if (message instanceof String) return trail((String) message);
        return new byte[0];
    }

    private static byte[] trail(String message) {
        return (message + "\n\n").getBytes();
    }

    private static FileChannel assertExists(FileChannel channel) {
        if (!channel.exists()) throw new RuntimeException("Channel does not exist");
        return channel;
    }

    private FileChannel assertNotExists(FileChannel channel) {
        if (channel.exists()) throw new RuntimeException("Channel already exists");
        return channel;
    }



}
