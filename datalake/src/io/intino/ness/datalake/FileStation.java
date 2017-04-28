package io.intino.ness.datalake;

import io.intino.ness.inl.FileMessageInputStream;
import io.intino.ness.inl.FileMessageOutputStream;
import io.intino.ness.inl.Message;
import io.intino.ness.inl.MessageInputStream;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static io.intino.ness.datalake.FileTank.Format.*;
import static java.nio.file.StandardOpenOption.*;
import static java.util.Arrays.*;

@SuppressWarnings("unchecked")
public class FileStation implements NessStation {

    private final File folder;
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
    public Tank tank(String tank) {
        return assertNotExists(get(tank)).create();
    }

    @Override
    public NessStation.Feed feed(String tank) {
        assertExists(get(tank));
        Feed feed = new Feed() {
            @Override
            public void send(Message message) {
                FileStation.this.send(message, tank);
            }

            @Override
            public String toString() {
                return "feed > " + tank;
            }
        };
        feedListTo(tank).add(feed);
        return feed;
    }

    @Override
    public Pipe pipe(String tank) {
        assertExists(get(tank));
        return createPipeFor(tank);
    }

    @Override
    public Flow flow(String tank) {
        assertExists(get(tank));
        Flow flow = new Flow() {
            List<Post> posts = new ArrayList<>();

            @Override
            public Flow to(Post post) {
                this.posts.add(post);
                return this;
            }

            @Override
            public void send(Message message) {
                posts.forEach(p -> p.send(message));
            }

            @Override
            public String toString() {
                return tank + " > flow";
            }
        };
        flowListFrom(tank).add(flow);
        return flow;
    }

    @Override
    public void remove(String tank) {
        assertExists(assertIsNotUsed(get(tank))).remove();
    }

    @Override
    public void remove(Feed... feeds) {
        for (Feed feed : feeds)
            this.feeds.values().forEach(list->list.remove(feed));
    }

    @Override
    public void remove(Pipe... pipes) {
        this.pipes.removeAll(Arrays.asList(pipes));
    }

    @Override
    public void remove(Flow... flows) {
        for (Flow flow : flows)
            this.flows.values().forEach(list->list.remove(flow));
    }

    @Override
    public Pump pump(String tank)  {
        return createPumpFor(tank);
    }

    @Override
    public Job seal(String tank) {
        return createSealTaskFor(assertExists(get(tank)));
    }


    @Override
    public Tank[] tanks() {
        return stream(tankFiles())
                .map(FileTank::new)
                .toArray(Tank[]::new);
    }

    @Override
    public Feed[] feedsTo(String tank) {
        return feedListTo(tank).toArray(new Feed[0]);
    }

    @Override
    public Pipe[] pipesFrom(String tank) {
        return pipes.stream()
                .filter(p -> tank.equalsIgnoreCase(p.from()))
                .toArray(Pipe[]::new);
    }

    @Override
    public Pipe[] pipesTo(String tank) {
        return pipes.stream()
                .filter(p -> tank.equalsIgnoreCase(p.to()))
                .toArray(Pipe[]::new);
    }

    @Override
    public Pipe pipeBetween(String source, String target) {
        return pipes.stream()
                .filter(p -> source.equalsIgnoreCase(p.from()) && target.equalsIgnoreCase(p.to()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Flow[] flowsFrom(String tank) {
        return flowListFrom(tank).toArray(new Flow[0]);
    }

    private List<Feed> feedListTo(String tank) {
        if (!feeds.containsKey(tank)) feeds.put(tank, new ArrayList<>());
        return feeds.get(tank);
    }

    private List<Flow> flowListFrom(String tank) {
        if (!flows.containsKey(tank)) flows.put(tank, new ArrayList<>());
        return flows.get(tank);
    }

    private List<Pipe> pipeListFrom(String tank) {
        return asList(pipesFrom(tank));
    }


    @Override
    public boolean exists(String tank) {
        return get(tank).exists();
    }

    @Override
    public void rename(String tank, String newName) {
        assertExists(get(tank)).rename(newName);
    }

    private File[] tankFiles() {
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

    private void send(Message message, String tank)  {
        try {
            write(message, tank);
            pipeListFrom(tank).forEach(pipe -> send(pipe.map(message), pipe.to()));
            flowListFrom(tank).forEach(flow -> flow.send(message));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void write(Message message, String tank) throws IOException {
        Path path = get(tank).fileOf(message, inl).toPath();
        Files.write(path, bytesOf(message), APPEND, CREATE);
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
            public Pipe to(String tank) {
                assertExists(get(tank));
                assertNotExistsPipeBetween(source,tank);
                this.target = tank;
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
            private Faucet faucet = new TankFaucet(get(source));
            private Map<String, FileTank> tanks = new HashMap<>();
            private List<Pipe> pipes = new ArrayList<>();
            private List<Post> posts = new ArrayList<>();

            @Override
            public Pump to(String tank) {
                tanks.put(tank, get(tank));
                pipes.add(pipeBetween(source, tank));
                return this;
            }

            @Override
            public Pump to(Post post) {
                posts.add(post);
                return this;
            }

            @Override
            public Job start() {

                return new Job() {

                    public boolean init() {
                        return pipes.size() > 0 || flows.size() > 0;
                    }

                    public boolean step() {
                        try {
                            Message message = faucet.next();
                            if (message == null) return false;
                            pipes.forEach(pipe -> targetTankOf(pipe).write(pipe.map(message)));
                            posts.forEach(post -> post.send(message));
                            return true;
                        } catch (IOException e) {
                            e.printStackTrace();
                            return false;
                        }
                    }

                    private FileTank targetTankOf(Pipe pipe) {
                        return tanks.get(pipe.to());
                    }

                    @Override
                    protected void onTerminate()  {
                        tanks.values().forEach(FileTank::close);
                        posts.forEach(Post::flush);
                    }


                };
            }
        };
    }

    private Job createSealTaskFor(final FileTank tank) {
        assertExists(tank);
        return new Job() {
            Iterator<File> iterator;

            @Override
            protected boolean init() {
                File[] files = tank.files(inl);
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

    private FileTank get(String tank) {
        return new FileTank(new File(this.folder, tank));
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

    private FileTank assertExists(FileTank tank) {
        if (!tank.exists()) throw new StationException("Tank " + tank.name() + "  does not exist");
        return tank;
    }

    private FileTank assertIsNotUsed(FileTank tank) {
        String name = tank.name();
        if (pipesFrom(name).length > 0) throw new StationException("Tank " + tank.name() + " is source of pipes. Remove pipes first");
        if (pipesTo(name).length > 0) throw new StationException("Tank " + tank.name() + " is target of pipes. Remove pipes first");
        if (feedsTo(name).length > 0) throw new StationException("Tank " + tank.name() + " is target of feeds. Remove feeds first");
        if (flowsFrom(name).length > 0) throw new StationException("Tank " + tank.name() + " is source of flows. Remove flows first");
        return tank;
    }

    private FileTank assertNotExists(FileTank tank) {
        if (tank.exists()) throw new StationException("Tank " + tank.name() + " already exists");
        return tank;
    }

    private void assertNotExistsPipeBetween(String source, String target) {
        if (pipeBetween(source,target) == null) return;
        throw new StationException("Pipe between " + source + " - " + target + " already exists");
    }


    private class StationException extends RuntimeException {
        StationException(String message) {
            super("DataLake Station: " + message);
        }
    }



}
