package io.intino.ness.datalake;

import io.intino.ness.inl.*;
import io.intino.ness.inl.FileMessageInputStream;
import io.intino.ness.inl.FileMessageOutputStream;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static io.intino.ness.datalake.FileTank.Format.inl;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

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
        feedsTo(tank).add(feed);
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
        flowsFrom(tank).add(flow);
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
    public Pump pump(String tank)  {
        return createPumpFor(tank);
    }

    @Override
    public Job seal(String tank) {
        return createSealTaskFor(assertExists(get(tank)));
    }


    @Override
    public List<Tank> tanks() {
        return stream(tankFiles())
                .map(FileTank::new)
                .collect(toList());
    }

    @Override
    public List<Feed> feedsTo(String tank) {
        if (feeds.containsKey(tank)) return feeds.get(tank);
        ArrayList<Feed> result =  new ArrayList<>();
        feeds.put(tank, result);
        return result;
    }

    @Override
    public List<Pipe> pipesFrom(String tank) {
        return pipes.stream()
                .filter(p -> tank.equalsIgnoreCase(p.from()))
                .collect(toList());
    }

    @Override
    public List<Pipe> pipesTo(String tank) {
        return pipes.stream()
                .filter(p -> tank.equalsIgnoreCase(p.to()))
                .collect(toList());
    }

    @Override
    public List<Pipe> pipesBetween(String source, String target) {
        return pipes.stream()
                .filter(p -> source.equalsIgnoreCase(p.from()) && target.equalsIgnoreCase(p.to()))
                .collect(toList());
    }

    @Override
    public List<Flow> flowsFrom(String tank) {
        if (flows.containsKey(tank)) return flows.get(tank);
        ArrayList<Flow> result =  new ArrayList<>();
        flows.put(tank, result);
        return result;
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
            pipesFrom(tank).forEach(pipe -> send(pipe.map(message), pipe.to()));
            flowsFrom(tank).forEach(flow -> flow.send(message));
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
                pipes.addAll(pipesBetween(source, tank));
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
                            pipes.forEach(pipe -> get(pipe).write(pipe.map(message)));
                            posts.forEach(post -> post.send(message));
                            return true;
                        } catch (IOException e) {
                            e.printStackTrace();
                            return false;
                        }
                    }

                    private FileTank get(Pipe pipe) {
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
        if (!tank.exists()) throw new StationException("Tank does not exist");
        return tank;
    }

    private FileTank assertIsNotUsed(FileTank tank) {
        String name = tank.name();
        if (pipesFrom(name).size() > 0) throw new StationException("Tank is source of pipes. Remove pipes first");
        if (pipesTo(name).size() > 0) throw new StationException("Tank is target of pipes. Remove pipes first");
        if (feedsTo(name).size() > 0) throw new StationException("Tank is target of feeds. Remove feeds first");
        if (flowsFrom(name).size() > 0) throw new StationException("Tank is source of flows. Remove flows first");
        return tank;
    }

    private FileTank assertNotExists(FileTank tank) {
        if (tank.exists()) throw new StationException("Tank already exists");
        return tank;
    }


    private class StationException extends RuntimeException {
        StationException(String message) {
            super("DataLake Station: " + message);
        }
    }



}
