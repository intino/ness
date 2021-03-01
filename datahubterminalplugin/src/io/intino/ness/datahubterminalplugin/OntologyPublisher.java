package io.intino.ness.datahubterminalplugin;

import io.intino.Configuration;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.graph.Datalake.Split;
import io.intino.datahub.graph.Datalake.Tank;
import io.intino.datahub.graph.Event;
import io.intino.datahub.graph.NessGraph;
import io.intino.datahub.graph.Wordbag;
import io.intino.itrules.FrameBuilder;
import io.intino.ness.datahubterminalplugin.event.EventRenderer;
import io.intino.ness.datahubterminalplugin.event.WordbagRenderer;
import io.intino.plugin.PluginLauncher;
import org.apache.maven.shared.invoker.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

class OntologyPublisher {
	private final File root;
	private final List<Tank.Event> eventTanks;
	private final List<Event> events;
	private final Configuration conf;
	private final List<File> resDirectories;
	private final List<File> sourceDirectories;
	private final Map<String, String> versions;
	private final PluginLauncher.SystemProperties systemProperties;
	private final String basePackage;
	private final PluginLauncher.Phase invokedPhase;
	private final PrintStream logger;
	private final StringBuilder errorStream;
	private final List<Wordbag> wordbags;

	OntologyPublisher(File root, NessGraph graph, Configuration configuration, PluginLauncher.ModuleStructure moduleStructure, Map<String, String> versions, PluginLauncher.SystemProperties systemProperties, PluginLauncher.Phase invokedPhase, PrintStream logger) {
		this.root = root;
		this.eventTanks = eventTanks(graph);
		this.events = graph.eventList();
		this.wordbags = graph.wordbagList();
		this.conf = configuration;
		this.resDirectories = moduleStructure.resDirectories;
		this.sourceDirectories = moduleStructure.sourceDirectories;
		this.versions = versions;
		this.systemProperties = systemProperties;
		this.basePackage = configuration.artifact().groupId().toLowerCase() + "." + Formatters.snakeCaseToCamelCase().format(configuration.artifact().name()).toString().toLowerCase();
		this.invokedPhase = invokedPhase;
		this.logger = logger;
		errorStream = new StringBuilder();
	}

	boolean publish() {
		try {
			if (!createSources()) return false;
			logger.println("Publishing ontology...");
			mvn(invokedPhase == PluginLauncher.Phase.INSTALL ? "install" : "deploy");
			logger.println("Ontology published!");
		} catch (Exception e) {
			logger.println(e.getMessage());
			logger.println(errorStream.toString());
			return false;
		}
		return true;
	}

	private boolean createSources() {
		File srcDirectory = sourceDirectory();
		srcDirectory.mkdirs();
		Map<Event, Split> eventSplitMap = splitEvents();
		eventSplitMap.forEach((k, v) -> new EventRenderer(k, v, srcDirectory, basePackage).render());
		events.stream().filter(event -> !eventSplitMap.containsKey(event)).parallel().forEach(event -> new EventRenderer(event, null, srcDirectory, basePackage).render());
		wordbags.stream().parallel().forEach(w -> new WordbagRenderer(w, conf, srcDirectory, resDirectories, basePackage).render());
		File resDirectory = new File(root, "res");
		resDirectory.mkdirs();
		wordbags.stream().filter(Wordbag::isInResource).map(Wordbag::asInResource).
				forEach(w -> {
					File source = new File(w.tsv().getPath());
					File destination = new File(resDirectory, relativeResource(source));
					destination.getParentFile().mkdirs();
					try {
						if (!destination.exists()) Files.copy(w.tsv().openStream(), destination.toPath());
					} catch (IOException e) {
						Logger.error(e);
					}
				});
		return true;
	}

	private File sourceDirectory() {
		return new File(root, "src");
	}

	private String relativeResource(File resourceFile) {
		String file = resourceFile.getAbsolutePath();
		for (File resDirectory : resDirectories) file = file.replace(resDirectory.getAbsolutePath(), "");
		return file;
	}

	private Map<Event, Split> splitEvents() {
		Map<Event, Split> events = new HashMap<>();
		for (Tank.Event tank : eventTanks) {
			List<Event> hierarchy = hierarchy(tank.event());
			Split split = tank.asTank().isSplitted() ? tank.asTank().asSplitted().split() : null;
			events.put(hierarchy.get(0), split);
			hierarchy.remove(0);
			hierarchy.forEach(e -> events.put(e, null));
		}
		return events;
	}

	private List<Event> hierarchy(Event event) {
		Set<Event> events = new LinkedHashSet<>();
		events.add(event);
		if (event.isExtensionOf()) events.addAll(hierarchy(event.asExtensionOf().parent()));
		return new ArrayList<>(events);
	}

	private void mvn(String goal) throws IOException, MavenInvocationException {
		final File pom = createPom(root, basePackage, conf.artifact().version());
		final InvocationResult result = invoke(pom, goal);
		if (result != null && result.getExitCode() != 0) {
			logger.println(errorStream.toString());
			if (result.getExecutionException() != null)
				throw new IOException("Failed to publish accessor.", result.getExecutionException());
			else throw new IOException("Failed to publish accessor. Exit code: " + result.getExitCode());
		} else if (result == null) throw new IOException("Failed to publish accessor. Maven HOME not found");
	}

	private InvocationResult invoke(File pom, String goal) throws MavenInvocationException {
		List<String> goals = new ArrayList<>();
		goals.add("clean");
		goals.add("install");
		if (!goal.isEmpty()) goals.add(goal);
		InvocationRequest request = new DefaultInvocationRequest().setPomFile(pom).setGoals(goals);
		Invoker invoker = new DefaultInvoker().setMavenHome(systemProperties.mavenHome);
		log(invoker);
		config(request, systemProperties.mavenHome);
		return invoker.execute(request);
	}

	private void log(Invoker invoker) {
		invoker.setErrorHandler(logger::println);
//		invoker.setOutputHandler(logger::println);
		invoker.setOutputHandler(s -> errorStream.append(s).append("\n"));
	}

	private void config(InvocationRequest request, File mavenHome) {
		final File mvn = new File(mavenHome, "bin" + File.separator + "mvn");
		mvn.setExecutable(true);
		request.setJavaHome(systemProperties.javaHome);
	}

	private File createPom(File root, String group, String version) {
		final FrameBuilder builder = new FrameBuilder("pom").add("group", group).add("artifact", "ontology").add("version", version);
		conf.repositories().forEach(r -> buildRepoFrame(builder, r));
		if (conf.artifact().distribution() != null) {
			if (isSnapshotVersion()) buildDistroFrame(builder, conf.artifact().distribution().snapshot());
			else buildDistroFrame(builder, conf.artifact().distribution().release());
		}
		builder.add("sourceDirectory", sourceDirectory().getAbsolutePath());
		for (File sourceDirectory : sourceDirectories)
			if (sourceDirectory.getName().equals("shared"))
				builder.add("sourceDirectory", sourceDirectory.getAbsolutePath());
		builder.add("event", new FrameBuilder().add("version", versions.get("event")));
		builder.add("led", new FrameBuilder().add("version", versions.get("led")));
		final File pomFile = new File(root, "pom.xml");
		Commons.write(pomFile.toPath(), new PomTemplate().render(builder.toFrame()));
		return pomFile;
	}

	private boolean isSnapshotVersion() {
		return conf.artifact().version().contains("SNAPSHOT");
	}

	private void buildRepoFrame(FrameBuilder builder, Configuration.Repository r) {
		builder.add("repository", createRepositoryFrame(r).toFrame());
	}

	private void buildDistroFrame(FrameBuilder builder, Configuration.Repository r) {
		builder.add("repository", createRepositoryFrame(r).add("distribution").toFrame());
	}

	private FrameBuilder createRepositoryFrame(Configuration.Repository repository) {
		return new FrameBuilder("repository", repository.getClass().getSimpleName()).
				add("name", repository.identifier()).
				add("random", UUID.randomUUID().toString()).
				add("url", repository.url());
	}

	private List<Tank.Event> eventTanks(NessGraph nessGraph) {
		if (nessGraph.datalake() == null) return Collections.emptyList();
		return nessGraph.datalake().tankList().stream().filter(Tank::isEvent).map(Tank::asEvent).collect(Collectors.toList());
	}
}
