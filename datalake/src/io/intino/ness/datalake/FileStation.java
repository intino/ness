package io.intino.ness.datalake;

import io.intino.ness.inl.Message;
import io.intino.ness.inl.MessageInputStream;
import io.intino.ness.inl.streams.FileMessageInputStream;
import io.intino.ness.inl.streams.FileMessageOutputStream;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

import static io.intino.ness.datalake.FileTank.Format.inl;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.time.Instant.parse;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;
import static org.slf4j.LoggerFactory.getLogger;

@SuppressWarnings("unchecked")
public class FileStation implements NessStation {
	private final File folder;
	private final Map<String, List<Feed>> feeds = new HashMap<>();
	private final Map<String, List<Drop>> drops = new HashMap<>();
	private final Map<String, List<Flow>> flows = new HashMap<>();
	private final List<Pipe> pipes = new ArrayList<>();

	public FileStation(String folder) {
		this(new File(folder));
	}

	public FileStation(File folder) {
		this.folder = folder;
	}

	private static File zipOf(File file) {
		return new File(file.getAbsolutePath().replace(".inl", ".zip"));
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
	public NessStation.Drop drop(String tank) {
		assertExists(get(tank));
		Drop drop = new Drop() {
			@Override
			public void register(Message message) {
				FileStation.this.register(message, tank);
			}

			@Override
			public String toString() {
				return "drop > " + tank;
			}
		};
		dropListTo(tank).add(drop);
		return drop;
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
			this.feeds.values().forEach(list -> list.remove(feed));
	}

	@Override
	public void remove(Pipe... pipes) {
		this.pipes.removeAll(Arrays.asList(pipes));
	}

	@Override
	public void remove(Flow... flows) {
		for (Flow flow : flows)
			this.flows.values().forEach(list -> list.remove(flow));
	}

	@Override
	public Pumping pump() {
		return createPumping();
	}

	@Override
	public PumpingTo pump(String tank) {
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

	private List<Drop> dropListTo(String tank) {
		if (!drops.containsKey(tank)) drops.put(tank, new ArrayList<>());
		return drops.get(tank);
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
		return zipFile.exists() ? new File[]{zipFile, file} : new File[]{file};
	}

	private void register(Message message, String tank) {
		try {
			write(message, tank);
		} catch (IOException e) {
			getLogger(ROOT_LOGGER_NAME).error(e.getMessage(), e);
		}
	}

	private void send(Message message, String tank) {
		try {
			write(message, tank);
			pipeListFrom(tank).forEach(pipe -> send(pipe.map(message), pipe.to()));
			flowListFrom(tank).forEach(flow -> flow.send(message));
		} catch (IOException e) {
			getLogger(ROOT_LOGGER_NAME).error(e.getMessage(), e);
		}
	}

	private void write(Message message, String tank) throws IOException {
		Path path = get(tank).fileOf(message, inl).toPath();
		Files.write(path, bytesOf(message), APPEND, CREATE);
	}

	private Pipe createPipeFor(final String source) {
		return new Pipe() {
			private String target;
			private Valve valve = new Valve();

			{
				pipes.add(this);
			}

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
				assertNotExistsPipeBetween(source, tank);
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

	private Pumping createPumping() {
		return new Pumping() {

			List<TankManager> managers = new ArrayList<>();
			List<LinkDef> links = new ArrayList<>();

			@Override
			public Link from(String source) {
				Pumping pumping = this;
				return new Link() {
					@Override
					public Pumping to(String target) {
						links.add(new TankLinkDef(source, target));
						return pumping;
					}

					@Override
					public Pumping to(Post target) {
						links.add(new PostLinkDef(source, target));
						return pumping;
					}
				};
			}

			@Override
			public Job asJob() {
				return new Job() {

					@Override
					protected boolean init() {
						if (links.isEmpty()) return false;
						managers = links.stream().map(l -> l.source).collect(toSet())
								.stream().map(TankManager::new).collect(toList());
						return true;
					}

					@Override
					protected boolean step() {
						if (!flowsAreActive(managers)) return false;
						TankManager manager = managerWithOldestMessage(managers);
						links.stream().filter(l -> l.source.equals(manager.source))
								.forEach(l -> l.send(manager.message));
						manager.next();
						return true;
					}

					@Override
					public void terminate() {
						super.terminate();
					}
				};
			}


			private TankManager managerWithOldestMessage(List<TankManager> managers) {
				Instant reference = instantOf(managers.get(0).message);
				TankManager manager = managers.get(0);
				for (int i = 1; i < managers.size(); i++) {
					Instant comparable = instantOf(managers.get(i).message);
					if (comparable.isBefore(reference)) {
						reference = comparable;
						manager = managers.get(i);
					}
				}
				return manager;
			}

			private Instant instantOf(io.intino.ness.inl.Message message) {
				return message != null ? parse(message.ts()) : Instant.MAX;
			}

			private boolean flowsAreActive(List<TankManager> managers) {
				for (TankManager manager : managers) if (manager.message != null) return true;
				return false;
			}

			abstract class LinkDef {

				String source;

				LinkDef(String source) {
					this.source = source;
				}

				abstract void send(Message message);

			}

			class TankLinkDef extends LinkDef {

				Pipe pipe;

				TankLinkDef(String source, String target) {
					super(source);
					this.pipe = pipeBetween(source, target);
				}

				@Override
				void send(Message message) {
					get(pipe.to()).write(message);
				}
			}

			class PostLinkDef extends LinkDef {

				Post target;

				PostLinkDef(String source, Post target) {
					super(source);
					this.target = target;
				}

				@Override
				void send(Message message) {
					target.send(message);
				}
			}

			class TankManager {
				private final String source;
				private final Faucet faucet;
				private io.intino.ness.inl.Message message;

				TankManager(String source) {
					this.source = source;
					this.faucet = new TankFaucet(get(source));
				}

				private void next() {
					try {
						this.message = faucet.next();
					} catch (IOException e) {
						getLogger(ROOT_LOGGER_NAME).error(e.getMessage(), e);
					}
				}
			}
		}

				;
	}

	private PumpingTo createPumpFor(String source) {
		return new PumpingTo() {
			private Faucet faucet = new TankFaucet(get(source));
			private Map<String, FileTank> tanks = new HashMap<>();
			private List<Pipe> pipes = new ArrayList<>();
			private List<Post> posts = new ArrayList<>();

			@Override
			public PumpingTo to(String tank) {
				tanks.put(tank, get(tank));
				pipes.add(pipeBetween(source, tank));
				return this;
			}

			@Override
			public PumpingTo to(Post post) {
				posts.add(post);
				return this;
			}

			@Override
			public Job asJob() {
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
							getLogger(ROOT_LOGGER_NAME).error(e.getMessage(), e);
							return false;
						}
					}

					@Override
					protected void onTerminate() {
						tanks.values().forEach(FileTank::close);
						posts.forEach(Post::flush);
					}

				};
			}

			private FileTank targetTankOf(Pipe pipe) {
				return tanks.get(pipe.to());
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
			protected boolean step() {
				if (!iterator.hasNext()) return false;
				try {
					seal(iterator.next());
					return true;
				} catch (IOException e) {
					getLogger(ROOT_LOGGER_NAME).error(e.getMessage(), e);
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

	private FileTank assertExists(FileTank tank) {
		if (!tank.exists()) throw new StationException("Tank " + tank.name() + "  does not exist");
		return tank;
	}

	private FileTank assertIsNotUsed(FileTank tank) {
		String name = tank.name();
		if (pipesFrom(name).length > 0)
			throw new StationException("Tank " + tank.name() + " is source of pipes. Remove pipes first");
		if (pipesTo(name).length > 0)
			throw new StationException("Tank " + tank.name() + " is target of pipes. Remove pipes first");
		if (feedsTo(name).length > 0)
			throw new StationException("Tank " + tank.name() + " is target of feeds. Remove feeds first");
		if (flowsFrom(name).length > 0)
			throw new StationException("Tank " + tank.name() + " is source of flows. Remove flows first");
		return tank;
	}

	private FileTank assertNotExists(FileTank tank) {
		if (tank.exists()) throw new StationException("Tank " + tank.name() + " already exists");
		return tank;
	}

	private void assertNotExistsPipeBetween(String source, String target) {
		if (pipeBetween(source, target) == null) return;
		throw new StationException("Pipe between " + source + " - " + target + " already exists");
	}


	private class StationException extends RuntimeException {
		StationException(String message) {
			super("DataLake Station: " + message);
		}
	}


}
