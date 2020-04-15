package io.intino.ness.datahubterminalplugin;

import com.google.gson.Gson;
import io.intino.Configuration;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.graph.Datalake.Context;
import io.intino.datahub.graph.Datalake.Tank;
import io.intino.datahub.graph.Event;
import io.intino.datahub.graph.Terminal;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.magritte.framework.Layer;
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
	private final PluginLauncher.SystemProperties systemProperties;
	private final String basePackage;
	private final PluginLauncher.Phase invokedPhase;
	private final PrintStream logger;
	private List<Tank.EventTankType> tanks;

	TerminalPublisher(File root, Terminal terminal, List<Tank.EventTankType> tanks, Configuration configuration, PluginLauncher.SystemProperties systemProperties, PluginLauncher.Phase invokedPhase, PrintStream logger) {
		this.root = root;
		this.terminal = terminal;
		this.tanks = tanks;
		this.conf = configuration;
		this.systemProperties = systemProperties;
		this.basePackage = configuration.artifact().groupId().toLowerCase() + "." + Formatters.snakeCaseToCamelCase().format(configuration.artifact().name()).toString().toLowerCase();
		this.invokedPhase = invokedPhase;
		this.logger = logger;
	}

	boolean publish() {
		if (!createSources()) return false;
		try {
			mvn(invokedPhase == PluginLauncher.Phase.INSTALL ? "install" : "deploy");
		} catch (IOException | MavenInvocationException e) {
			logger.println(e.getMessage());
			return false;
		}
		return true;
	}

	private boolean createSources() {
		File srcDirectory = new File(root, "src");
		srcDirectory.mkdirs();
		Map<Event, Context> eventContextMap = collectEvents(tanks);
		new TerminalRenderer(terminal, eventContextMap, srcDirectory, basePackage).render();
		File resDirectory = new File(root, "res");
		resDirectory.mkdirs();
		writeManifest(resDirectory);
		return true;
	}

	private void writeManifest(File srcDirectory) {
		List<String> publish = terminal.publish() != null ? terminal.publish().tanks().stream().map(t -> t.event().name$()).collect(Collectors.toList()) : Collections.emptyList();
		List<String> subscribe = terminal.subscribe() != null ? terminal.subscribe().tanks().stream().map(t -> t.event().name$()).collect(Collectors.toList()) : Collections.emptyList();
		Manifest manifest = new Manifest(terminal.name$(), basePackage + "." + Formatters.firstUpperCase(Formatters.snakeCaseToCamelCase().format(terminal.name$()).toString()), publish, subscribe, tankClasses(), eventContexts());
		try {
			Files.write(new File(srcDirectory, "terminal.mf").toPath(), new Gson().toJson(manifest).getBytes());
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private String terminalNameArtifact() {
		return Formatters.firstLowerCase(Formatters.camelCaseToSnakeCase().format(terminal.name$()).toString());
	}

	private Map<String, Set<String>> eventContexts() {
		Map<String, Set<String>> eventContexts = terminal.publish() == null ? new HashMap<>() : eventContextOf(terminal.publish().tanks());
		if (terminal.subscribe() == null) return eventContexts;
		Map<String, Set<String>> subscribeEventContexts = eventContextOf(terminal.subscribe().tanks());
		for (String eventType : subscribeEventContexts.keySet()) {
			if (!eventContexts.containsKey(eventType)) eventContexts.put(eventType, new HashSet<>());
			eventContexts.get(eventType).addAll(subscribeEventContexts.get(eventType));
		}
		return eventContexts;
	}

	private Map<String, Set<String>> eventContextOf(List<Tank.EventTankType> tanks) {
		return tanks.stream().
				collect(Collectors.toMap(t -> t.event().name$(),
						tank -> tank.asTank().isContextual() ? tank.asTank().asContextual().context().leafs().stream().map(Context::qn).collect(Collectors.toSet()) : Collections.emptySet(),
						(a, b) -> b));
	}

	private Map<String, String> tankClasses() {
		Map<String, String> tankClasses = new HashMap<>();
		if (terminal.publish() != null)
			terminal.publish().tanks().forEach(t -> tankClasses.putIfAbsent(t.event().name$(), basePackage + ".events." + t.event().name$()));
		if (terminal.subscribe() != null)
			terminal.subscribe().tanks().forEach(t -> tankClasses.putIfAbsent(t.event().name$(), basePackage + ".events." + t.event().name$()));
		if (terminal.allowsBpmIn() != null) {
			Context context = terminal.allowsBpmIn().context();
			String statusQn = terminal.allowsBpmIn().processStatusClass();
			String statusClassName = statusQn.substring(statusQn.lastIndexOf(".") + 1);
			tankClasses.put((context != null ? context.qn() + "." : "") + statusClassName, statusQn);
		}
		return tankClasses;
	}

	private Map<Event, Context> collectEvents(List<Tank.EventTankType> tanks) {
		Map<Event, Context> events = new HashMap<>();
		for (Tank.EventTankType tank : tanks) {
			List<Event> hierarchy = hierarchy(tank.event());
			Context context = tank.asTank().isContextual() ? tank.asTank().asContextual().context() : null;
			events.put(hierarchy.get(0), context);
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
		logger.println("Maven HOME: " + systemProperties.mavenHome.getAbsolutePath());
		Invoker invoker = new DefaultInvoker().setMavenHome(systemProperties.mavenHome);
		log(invoker);
		config(request, systemProperties.mavenHome);
		return invoker.execute(request);
	}

	private void log(Invoker invoker) {
		invoker.setErrorHandler(logger::println);
		invoker.setOutputHandler(logger::println);
	}

	private void config(InvocationRequest request, File mavenHome) {
		final File mvn = new File(mavenHome, "bin" + File.separator + "mvn");
		mvn.setExecutable(true);
		request.setJavaHome(systemProperties.javaHome);
	}


	private File createPom(File root, String group, String artifact, String version) {
		final FrameBuilder builder = new FrameBuilder("pom").add("group", group).add("artifact", artifact).add("version", version);
		conf.repositories().stream().filter(r -> !(r instanceof Configuration.Repository.Language)).forEach(r -> buildRepoFrame(builder, r, conf.artifact().version().contains("SNAPSHOT")));
		builder.add("ontology", new FrameBuilder("ontology").add("group", group).add("artifact", "ontology").add("version", version));
		if (terminal.allowsBpmIn() != null) builder.add("hasBpm", ";");
		final File pomFile = new File(root, "pom.xml");
		Commons.write(pomFile.toPath(), new AccessorPomTemplate().render(builder.toFrame()));
		return pomFile;
	}

	private void buildRepoFrame(FrameBuilder builder, Configuration.Repository r, boolean snapshot) {
		builder.add("repository", createRepositoryFrame(r, snapshot));
	}

	private Frame createRepositoryFrame(Configuration.Repository repository, boolean snapshot) {
		return new FrameBuilder("repository", isDistribution(repository, snapshot) ? "distribution" : "release").
				add("name", repository.identifier()).
				add("random", UUID.randomUUID().toString()).
				add("url", repository.url()).toFrame();
	}

	private boolean isDistribution(Configuration.Repository repository, boolean snapshot) {
		Configuration.Distribution distribution = conf.artifact().distribution();
		if (distribution == null) return false;
		Configuration.Repository repo = snapshot ? distribution.snapshot() : distribution.release();
		return repo != null && repository.identifier().equals(repo.identifier()) &&
				repository.url().equals(repo.url());
	}
}
