package io.intino.ness.datalake;

import io.intino.ness.datalake.NessDataLake.Joint;
import io.intino.ness.inl.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.intino.ness.datalake.FileChannel.Format.*;

import static java.nio.file.StandardOpenOption.*;
import static java.util.Arrays.stream;

public class FilePumpingStation implements NessPumpingStation {

    private final File folder;
    private final Map<String, List<Pipe>> pipes = new HashMap<>();
    private Map<String, Joint> joints = new HashMap<>();

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

    @Override
    public Pipe feed(String channel) {
        return new Pipe() {
            @Override
            public void send(Message message) {
                write(channel, message);
                pipes.get(channel).forEach(p->p.send(message));
            }

            @Override
            public void flush() {

            }
        };
    }

    private void write(String channel, Message message)  {
        try {
            File file = get(channel).fileOf(message,inl);
            write(file, message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void write(File file, Object message) throws IOException {
        Files.write(file.toPath(), bytesOf(message), APPEND, CREATE);
    }

    @Override
    public Channeling pipe(String channel) {
        return new Channeling() {

            private List<MessageFunction> functions = new ArrayList<>();

            @Override
            public Channeling with(String function) throws Exception {
                return with(classOf(function));
            }

            @Override
            public Channeling with(String function, String... sources) throws Exception {
                return with(compile(function, sources));
            }

            @Override
            public Channeling with(Class<? extends MessageFunction> functionClass) throws Exception {
                return with(functionClass.newInstance());
            }

            @Override
            public Channeling with(MessageFunction function) {
                this.functions.add(function);
                return this;
            }

            @Override
            public Pipe to(String target) {
                return put(channel, pipeOf(target));
            }

            @Override
            public Pipe to(Pipe... pipes) {
                return put(channel, wrap(pipes));
            }

            @Override
            public void join(Joint joint) {
                joints.put(channel, joint);
            }

            private Pipe pipeOf(String target) {
                FileChannel store = get(target);
                return new Pipe() {
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

            private Pipe wrap(Pipe[] pipes) {
                return new Pipe() {
                    @Override
                    public void send(Message message) {
                        Message cast = cast(message);
                        if (cast == null) return;
                        stream(pipes).forEach(p -> p.send(cast));
                    }

                    @Override
                    public void flush() {
                        stream(pipes).forEach(Pipe::flush);
                    }
                };
            }

            private Message cast(Message message) {
                for (MessageFunction function : functions) {
                    if (message == null) return null;
                    message = function.cast(message);
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
        return start(createPumpTaskFor(channel));
    }

    @Override
    public Task seal(String channel) {
        return start(createSealTaskFor(channel));
    }

    private Task start(Task task) {
        task.thread().start();
        return task;
    }

    private Task createPumpTaskFor(String channel) throws Exception {

        return new Task() {
            Thread thread = new Thread(this);
            boolean running = true;
            NessFaucet faucet = new NessFaucet(get(channel));
            List<Pipe> pipes = FilePumpingStation.this.pipes.get(channel);

            @Override
            public Thread thread() {
                return thread;
            }

            @Override
            public void run() {
                while (running) try {
                    running = step();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                pipes.forEach(Pipe::flush);
            }

            public boolean step() throws IOException {
                Message message = faucet.next();
                if (message == null || pipes == null || pipes.size() == 0) return false;
                pipes.forEach(p -> p.send(message));
                return true;
            }

            @Override
            public void stop() {
                running = false;
            }

        };
    }

    private Task createSealTaskFor(final String channel) {
        return new Task() {
            Thread thread = new Thread(this);
            FileChannel fileChannel = assertExists(get(channel));

            @Override
            public Thread thread() {
                return thread;
            }

            @Override
            public void run() {
                stream(fileChannel.files(inl))
                        .parallel()
                        .forEach(this::seal);
            }

            private void seal(File file)  {
                try {
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

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            @Override
            public void stop() {

            }


        };
    }

    @SuppressWarnings("unchecked")
    private Class<MessageFunction> classOf(String name) throws ClassNotFoundException {
        return (Class<MessageFunction>) Class.forName(name);
    }

    private Class<MessageFunction> compile(String function, String... sources) {
        return NessCompiler.compile(sources)
                .with("-target", "1.8")
                .load(function)
                .as(MessageFunction.class);
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

    private static FileChannel assertExists(FileChannel store) {
        if (!store.exists()) throw new RuntimeException("Channel does not exist");
        return store;
    }

    private FileChannel assertNotExists(FileChannel store) {
        if (store.exists()) throw new RuntimeException("Channel already exists");
        return store;
    }

}
