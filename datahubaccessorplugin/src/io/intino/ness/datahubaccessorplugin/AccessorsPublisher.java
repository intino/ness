package io.intino.ness.datahubaccessorplugin;

import io.intino.datahub.graph.Datalake.Tank;
import io.intino.datahub.graph.MessageHub;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.legio.graph.LegioGraph;
import io.intino.legio.graph.Repository;
import io.intino.ness.datahubaccessorplugin.schema.MessageRenderer;
import io.intino.plugin.PluginLauncher;
import org.apache.maven.shared.invoker.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class AccessorsPublisher {
	private final File root;
	private final MessageHub messageHub;
	private final LegioGraph conf;
	private final PluginLauncher.SystemProperties systemProperties;
	private final String basePackage;
	private final PrintStream logger;
	private List<Tank.Event> tanks;

	AccessorsPublisher(File root, MessageHub messageHub, List<Tank.Event> tanks, LegioGraph configuration, PluginLauncher.SystemProperties systemProperties, PrintStream logger) {
		this.root = root;
		this.messageHub = messageHub;
		this.tanks = tanks;
		this.conf = configuration;
		this.systemProperties = systemProperties;
		this.basePackage = configuration.artifact().groupId();
		this.logger = logger;
	}

	void publish() {
		if (!createSources()) return;
		try {
			mvn("install");
		} catch (IOException | MavenInvocationException e) {
			logger.println(e.getMessage());
		}
	}

	private boolean createSources() {
		File srcDirectory = new File(root, "src");
		srcDirectory.mkdirs();
		new MessageHubRenderer(messageHub, srcDirectory, basePackage).render();
		tanks.forEach(t -> new MessageRenderer(t.message(), srcDirectory, basePackage).render());
		return true;
	}

	private void mvn(String goal) throws IOException, MavenInvocationException {
		final File pom = createPom(root, basePackage, messageHub.name$() + "MessageHub", conf.artifact().version());
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
		conf.repositoryList().forEach(r -> buildRepoFrame(builder, r));
		final FrameBuilder depBuilder = new FrameBuilder(messageHub.core$().conceptList().stream().map(s -> s.id().split("#")[0].toLowerCase()).toArray(String[]::new)).add("value", "");
		builder.add("dependency", depBuilder.toFrame());
		final File pomFile = new File(root, "pom.xml");
		Commons.write(pomFile.toPath(), new AccessorPomTemplate().render(builder.toFrame()));
		return pomFile;
	}

	private void buildRepoFrame(FrameBuilder builder, Repository r) {
		builder.add("repository", r.releaseList().stream().map(release -> createRepositoryFrame(r, release)).toArray(Frame[]::new));
	}

	private Frame createRepositoryFrame(Repository repository, Repository.Release release) {
		return new FrameBuilder("repository", isDistribution(release) ? "distribution" : "release").
				add("name", repository.identifier()).
				add("random", UUID.randomUUID().toString()).
				add("url", release.url()).toFrame();
	}

	private boolean isDistribution(Repository.Release release) {
		return conf.artifact().distribution() != null && release.equals(conf.artifact().distribution().release());
	}
}
