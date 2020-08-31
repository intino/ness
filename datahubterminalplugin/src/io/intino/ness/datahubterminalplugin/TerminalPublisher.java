package io.intino.ness.datahubterminalplugin;

import com.google.gson.Gson;
import io.intino.Configuration;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.graph.Datalake;
import io.intino.datahub.graph.Datalake.Split;
import io.intino.datahub.graph.Datalake.Tank;
import io.intino.datahub.graph.Event;
import io.intino.datahub.graph.Namespace;
import io.intino.datahub.graph.Terminal;
import io.intino.itrules.FrameBuilder;
import io.intino.plugin.PluginLauncher;
import org.apache.maven.shared.invoker.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

class TerminalPublisher {
	private final File root;
	private final Terminal terminal;
	private final Configuration conf;
	private final String terminalJmsVersion;
	private final String bpmVersion;
	private final PluginLauncher.SystemProperties systemProperties;
	private final String basePackage;
	private final PluginLauncher.Phase invokedPhase;
	private final PrintStream logger;
	private final List<Tank.Event> tanks;

	TerminalPublisher(File root, Terminal terminal, List<Tank.Event> tanks, Configuration configuration, String terminalJmsVersion, String bpmVersion, PluginLauncher.SystemProperties systemProperties, PluginLauncher.Phase invokedPhase, PrintStream logger) {
		this.root = root;
		this.terminal = terminal;
		this.tanks = tanks;
		this.conf = configuration;
		this.terminalJmsVersion = terminalJmsVersion;
		this.bpmVersion = bpmVersion;
		this.systemProperties = systemProperties;
		this.basePackage = configuration.artifact().groupId().toLowerCase() + "." + Formatters.snakeCaseToCamelCase().format(configuration.artifact().name()).toString().toLowerCase();
		this.invokedPhase = invokedPhase;
		this.logger = logger;
	}

	boolean publish() {
		if (!createSources()) return false;
		try {
			logger.println("Publishing " + terminal.name$() + "...");
			mvn(invokedPhase == PluginLauncher.Phase.INSTALL ? "install" : "deploy");
			logger.println("Terminal " + terminal.name$() + " published!");
		} catch (Exception e) {
			logger.println(e.getMessage());
			return false;
		}
		return true;
	}

	private boolean createSources() {
		File srcDirectory = new File(root, "src");
		srcDirectory.mkdirs();
		Map<Event, Datalake.Split> eventSplitMap = collectEvents(tanks);
		new TerminalRenderer(terminal, eventSplitMap, srcDirectory, basePackage).render();
		File resDirectory = new File(root, "res");
		resDirectory.mkdirs();
		writeManifest(resDirectory);
		return true;
	}

	private void writeManifest(File srcDirectory) {
		List<String> publish = terminal.publish() != null ? terminal.publish().tanks().stream().map(this::eventQn).collect(Collectors.toList()) : Collections.emptyList();
		List<String> subscribe = terminal.subscribe() != null ? terminal.subscribe().tanks().stream().map(this::eventQn).collect(Collectors.toList()) : Collections.emptyList();
		Manifest manifest = new Manifest(terminal.name$(), basePackage + "." + Formatters.firstUpperCase(Formatters.snakeCaseToCamelCase().format(terminal.name$()).toString()), publish, subscribe, tankClasses(), eventSplits());
		try {
			Files.write(new File(srcDirectory, "terminal.mf").toPath(), new Gson().toJson(manifest).getBytes());
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private String namespace(Event event) {
		return event.core$().owner().is(Namespace.class) ? event.core$().owner().name() + "." : "";
	}

	private String terminalNameArtifact() {
		return Formatters.firstLowerCase(Formatters.camelCaseToSnakeCase().format(terminal.name$()).toString());
	}

	private Map<String, Set<String>> eventSplits() {
		Map<String, Set<String>> eventSplits = terminal.publish() == null ? new HashMap<>() : eventSplitOf(terminal.publish().tanks());
		if (terminal.subscribe() == null) return eventSplits;
		Map<String, Set<String>> subscribeEventSplits = eventSplitOf(terminal.subscribe().tanks());
		for (String eventType : subscribeEventSplits.keySet()) {
			if (!eventSplits.containsKey(eventType)) eventSplits.put(eventType, new HashSet<>());
			eventSplits.get(eventType).addAll(subscribeEventSplits.get(eventType));
		}
		return eventSplits;
	}

	private Map<String, Set<String>> eventSplitOf(List<Tank.Event> tanks) {
		return tanks.stream().
				collect(Collectors.toMap(this::eventQn,
						tank -> tank.asTank().isSplitted() ? tank.asTank().asSplitted().split().leafs().stream().map(Split::qn).collect(Collectors.toSet()) : Collections.emptySet(), (a, b) -> b));
	}

	private Map<String, String> tankClasses() {
		Map<String, String> tankClasses = new HashMap<>();
		if (terminal.publish() != null)
			terminal.publish().tanks().forEach(t -> tankClasses.putIfAbsent(eventQn(t), basePackage + ".events." + namespace(t.event()).toLowerCase() + t.event().name$()));
		if (terminal.subscribe() != null)
			terminal.subscribe().tanks().forEach(t -> tankClasses.putIfAbsent(eventQn(t), basePackage + ".events." + namespace(t.event()).toLowerCase() + t.event().name$()));
		if (terminal.bpm() != null) {
			Split split = terminal.bpm().split();
			String statusQn = terminal.bpm().processStatusClass();
			String statusClassName = statusQn.substring(statusQn.lastIndexOf(".") + 1);
			tankClasses.put((split != null ? split.qn() + "." : "") + statusClassName, statusQn);
		}
		return tankClasses;
	}

	private String eventQn(Tank.Event t) {
		return namespace(t.event()) + t.event().name$();
	}

	private Map<Event, Split> collectEvents(List<Tank.Event> tanks) {
		Map<Event, Split> events = new HashMap<>();
		for (Tank.Event tank : tanks) {
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
		final File pom = createPom(root, basePackage, terminalNameArtifact(), conf.artifact().version());
		final InvocationResult result = invoke(pom, goal);
		if (result != null && result.getExitCode() != 0) {
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
		invoker.setErrorHandler(l -> {
			if (!l.startsWith("[WARN]")) logger.println(l);
		});
		invoker.setOutputHandler(s -> {
		});
	}

	private void config(InvocationRequest request, File mavenHome) {
		final File mvn = new File(mavenHome, "bin" + File.separator + "mvn");
		mvn.setExecutable(true);
		request.setJavaHome(systemProperties.javaHome);
	}


	private File createPom(File root, String group, String artifact, String version) {
		final FrameBuilder builder = new FrameBuilder("pom").add("group", group).add("artifact", artifact).add("version", version);
		conf.repositories().forEach(r -> buildRepoFrame(builder, r));
		if (conf.artifact().distribution() != null) {
			if (isSnapshotVersion()) buildDistroFrame(builder, conf.artifact().distribution().snapshot());
			else buildDistroFrame(builder, conf.artifact().distribution().release());
		}
		builder.add("ontology", ontologyFrame(group, version));
		if (terminal.bpm() != null) builder.add("hasBpm", this.bpmVersion);
		final File pomFile = new File(root, "pom.xml");
		Commons.write(pomFile.toPath(), new AccessorPomTemplate().render(builder.toFrame()));
		return pomFile;
	}

	private boolean isSnapshotVersion() {
		return conf.artifact().version().contains("SNAPSHOT");
	}

	private FrameBuilder ontologyFrame(String group, String version) {
		return new FrameBuilder("ontology").
				add("group", group).
				add("artifact", "ontology").
				add("terminalVersion", terminalJmsVersion).
				add("version", version);
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

}