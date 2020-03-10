package io.intino.ness.datahubterminalplugin;

import io.intino.Configuration;
import io.intino.Configuration.Repository;
import io.intino.datahub.graph.Datalake.Context;
import io.intino.datahub.graph.Datalake.Tank;
import io.intino.datahub.graph.Event;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.ness.datahubterminalplugin.event.EventRenderer;
import io.intino.plugin.PluginLauncher;
import org.apache.maven.shared.invoker.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

class OntologyPublisher {
	private final File root;
	private final List<Event> events;
	private final Configuration conf;
	private final PluginLauncher.SystemProperties systemProperties;
	private final String basePackage;
	private final PluginLauncher.Phase invokedPhase;
	private final PrintStream logger;
	private List<Tank.Event> tanks;

	OntologyPublisher(File root, List<Tank.Event> tanks, List<Event> events, Configuration configuration, PluginLauncher.SystemProperties systemProperties, PluginLauncher.Phase invokedPhase, PrintStream logger) {
		this.root = root;
		this.tanks = tanks;
		this.events = events;
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
		eventContextMap.forEach((k, v) -> new EventRenderer(k, v, srcDirectory, basePackage).render());
		events.stream().filter(event -> !eventContextMap.containsKey(event)).forEach(event -> new EventRenderer(event, null, srcDirectory, basePackage).render());
		File resDirectory = new File(root, "res");
		resDirectory.mkdirs();
		return true;
	}


	private Map<Event, Context> collectEvents(List<Tank.Event> tanks) {
		Map<Event, Context> events = new HashMap<>();
		for (Tank.Event tank : tanks) {
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
		final File pom = createPom(root, basePackage, conf.artifact().version());
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

	private File createPom(File root, String group, String version) {
		final FrameBuilder builder = new FrameBuilder("pom").add("group", group).add("artifact", "ontology").add("version", version);
		conf.repositories().stream().filter(r -> r instanceof Repository.Release).forEach(r -> buildRepoFrame(builder, r, conf.artifact().version().contains("SNAPSHOT")));
		builder.add("event", new FrameBuilder());
		final File pomFile = new File(root, "pom.xml");
		Commons.write(pomFile.toPath(), new AccessorPomTemplate().render(builder.toFrame()));
		return pomFile;
	}

	private void buildRepoFrame(FrameBuilder builder, Repository r, boolean snapshot) {
		builder.add("repository", createRepositoryFrame(r, snapshot));
	}

	private Frame createRepositoryFrame(Repository repository, boolean snapshot) {
		return new FrameBuilder("repository", isDistribution(repository, snapshot) ? "distribution" : "release").
				add("name", repository.identifier()).
				add("random", UUID.randomUUID().toString()).
				add("url", repository.url()).toFrame();
	}

	private boolean isDistribution(Repository repository, boolean snapshot) {
		Configuration.Distribution distribution = conf.artifact().distribution();
		if (distribution == null) return false;
		Repository repo = snapshot ? distribution.snapshot() : distribution.release();
		return repo != null && repository.identifier().equals(repo.identifier()) &&
				repository.url().equals(repo.url());
	}
}
