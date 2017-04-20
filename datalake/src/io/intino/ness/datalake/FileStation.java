package io.intino.ness.datalake;

import io.intino.ness.datalake.NessDataLake.Joint;
import io.intino.ness.inl.*;
import io.intino.ness.inl.FileMessageInputStream;
import io.intino.ness.inl.FileMessageOutputStream;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static io.intino.ness.datalake.FileChannel.Format.inl;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

@SuppressWarnings("unchecked")
public class FileStation implements NessStation {

    private final File folder;

    private final Map<String, Joint> joints = new HashMap<>();
    private final Map<String, List<Feed>> feeds = new HashMap<>();
    private final Map<String, List<Flow>> flows = new HashMap<>();
    private final List<Pipe> pipes = new ArrayList<>();

    public FileStation(String folder) {
        this(new File(folder));
    }

    public FileStation(File folder) {
        this.folder = folder;
    }

    @Override
    public boolean exists(String channel) {
        return get(channel).exists();
    }

    @Override
    public List<String> channels() {
        return stream(channelFiles())
                .map(File::getName)
                .collect(toList());
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
    public void settle(String channel, Joint joint) {
        joints.put(channel, joint);
    }

    @Override
    public NessStation.Feed feed(String channel) {
        Feed feed = new Feed() {
            @Override
            public void send(Message message) {
                FileStation.this.send(message, channel);
            }

            @Override
            public String toString() {
                return "feed > " + channel;
            }
        };
        feedsTo(channel).add(feed);
        return feed;
    }

    @Override
    public Pipe pipe(String channel) {
        return createPipeFor(channel);
    }

    @Override
    public Flow flow(String channel) {
        Flow flow = new Flow() {
            List<Post> posts = new ArrayList<>();

            @Override
            public Flow onMessage(Post post) {
                this.posts.add(post);
                return this;
            }

            @Override
            public void send(Message message) {
                posts.forEach(p -> p.send(message));
            }

            @Override
            public String toString() {
                return channel + " > flow";
            }
        };
        flowsFrom(channel).add(flow);
        return flow;
    }

    @Override
    public void remove(Feed... feeds) {
        for (Feed feed : feeds)
            this.feeds.values().forEach(list->list.remove(feed));
    }

    @Override
    public void remove(Pipe... pipes) {
        for (Pipe pipe : pipes) {
            this.pipes.remove(pipe);
        }
    }

    @Override
    public void remove(Flow... flows) {
        for (Flow flow : flows)
            this.flows.values().forEach(list->list.remove(flow));
    }


    @Override
    public Pump pump(String channel)  {
        return createPumpFor(channel);
    }

    private File[] channelFiles() {
        File[] files = folder.listFiles(File::isDirectory);
        return files != null ? files : new File[0];
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private File[] setOf(File file) {
        File zipFile = zipOf(file);
        return zipFile.exists() ? new File[] {zipFile, file} : new File[] {file};
    }

    private static File zipOf(File file) {
        return new File(file.getAbsolutePath().replace(".inl",".zip"));
    }

    private void send(Message message, String channel)  {
        try {
            write(message, channel);
            pipesFrom(channel).forEach(pipe -> send(pipe.map(message), pipe.to()));
            flowsFrom(channel).forEach(flow -> flow.send(message));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void write(Message message, String channel) throws IOException {
        Path path = get(channel).fileOf(message,inl).toPath();
        Files.write(path, bytesOf(message), APPEND, CREATE);
    }

    @Override
    public List<Feed> feedsTo(String channel) {
        if (feeds.containsKey(channel)) return feeds.get(channel);
        ArrayList<Feed> result =  new ArrayList<>();
        feeds.put(channel, result);
        return result;
    }

    @Override
    public List<Pipe> pipesFrom(String channel) {
        return pipes.stream()
                .filter(p -> channel.equalsIgnoreCase(p.from()))
                .collect(toList());
    }

    @Override
    public List<Pipe> pipesTo(String channel) {
        return pipes.stream()
                .filter(p -> channel.equalsIgnoreCase(p.to()))
                .collect(toList());
    }

    @Override
    public List<Pipe> pipesBetween(String source, String target) {
        return pipes.stream()
                .filter(p -> source.equalsIgnoreCase(p.from()) && target.equalsIgnoreCase(p.to()))
                .collect(toList());
    }

    @Override
    public List<Flow> flowsFrom(String channel) {
        if (flows.containsKey(channel)) return flows.get(channel);
        ArrayList<Flow> result =  new ArrayList<>();
        flows.put(channel, result);
        return result;
    }

    @Override
    public Task seal(String channel) {
        return createSealTaskFor(assertExists(get(channel)));
    }

    private Pipe createPipeFor(final String source) {
        return new Pipe() {
            { pipes.add(this); }
            private String target;

            private Valve valve = new Valve();
            @Override
            public String from() {
                return source;
            }

            @Override
            public String to() {
                return target;
            }

            @Override
            public Message map(Message message) {
                return valve != null ? valve.map(message) : message;
            }

            @Override
            public Pipe to(String channel) {
                this.target = channel;
                return this;
            }

            @Override
            public Pipe with(Valve valve) {
                this.valve = valve;
                return this;
            }

            @Override
            public String toString() {
                return source + " > " + valve.toString() + " > " + target;
            }

        };
    }

    private Pump createPumpFor(String source) {
        return new Pump() {
            private NessFaucet faucet = new NessFaucet(get(source));
            private Map<String, FileChannel> channels = new HashMap<>();
            private List<Pipe> pipes = new ArrayList<>();
            private List<Post> posts = new ArrayList<>();

            @Override
            public Pump to(String channel) {
                channels.put(channel, get(channel));
                pipes.addAll(pipesBetween(source,channel));
                return this;
            }

            @Override
            public Pump to(Post post) {
                posts.add(post);
                return this;
            }

            @Override
            public Task start() {

                return new Task() {

                    public boolean init() {
                        return pipes != null && pipes.size() != 0;
                    }

                    public boolean step() {
                        try {
                            Message message = faucet.next();
                            if (message == null) return false;
                            pipes.forEach(pipe -> get(pipe).write(message));
                            posts.forEach(post -> post.send(message));
                            return true;
                        } catch (IOException e) {
                            e.printStackTrace();
                            return false;
                        }
                    }

                    private FileChannel get(Pipe pipe) {
                        return channels.get(pipe.to());
                    }

                    @Override
                    protected void onTerminate()  {
                        channels.values().forEach(FileChannel::close);
                        posts.forEach(Post::flush);
                    }


                };
            }
        };
    }

    private Task createSealTaskFor(final FileChannel channel) {
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

            @SuppressWarnings("ResultOfMethodCallIgnored")
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
