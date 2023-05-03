package io.intino.ness.datahubterminalplugin.ontology;

import io.intino.Configuration;
import io.intino.datahub.model.NessGraph;
import io.intino.itrules.FrameBuilder;
import io.intino.ness.datahubterminalplugin.ArtifactoryConnector;
import io.intino.ness.datahubterminalplugin.Commons;
import io.intino.ness.datahubterminalplugin.Formatters;
import io.intino.ness.datahubterminalplugin.PomTemplate;
import io.intino.plugin.PluginLauncher;
import org.apache.maven.shared.invoker.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.intino.plugin.PluginLauncher.Phase.DISTRIBUTE;

public class OntologyPublisher {
	private final File root;
	private final NessGraph graph;
	private final Configuration conf;
	private final List<File> sourceDirectories;
	private final Map<String, String> versions;
	private final PluginLauncher.SystemProperties systemProperties;
	private final String basePackage;
	private final PluginLauncher.Phase invokedPhase;
	private final PrintStream logger;
	private final PluginLauncher.Notifier notifier;
	private final StringBuilder errorStream;

	public OntologyPublisher(File root, NessGraph graph, Configuration configuration, PluginLauncher.ModuleStructure moduleStructure, Map<String, String> versions, PluginLauncher.SystemProperties systemProperties, PluginLauncher.Phase invokedPhase, PrintStream logger, PluginLauncher.Notifier notifier) {
		this.root = root;
		this.graph = graph;
		this.conf = configuration;
		this.sourceDirectories = moduleStructure.sourceDirectories;
		this.versions = versions;
		this.systemProperties = systemProperties;
		this.basePackage = configuration.artifact().groupId().toLowerCase() + "." + Formatters.snakeCaseToCamelCase().format(configuration.artifact().name()).toString().toLowerCase();
		this.invokedPhase = invokedPhase;
		this.logger = logger;
		this.notifier = notifier;
		this.errorStream = new StringBuilder();
	}

	public boolean publish() {
		try {
			if (invokedPhase.equals(DISTRIBUTE) && !isSnapshotVersion() && isDistributed(conf.artifact())) {
				logger.println("This Version Already Exists");
				notifier.notifyError("The Version " + conf.artifact().version() + " is Already Distributed.");
				return false;
			}
			if (!new OntologyRenderer(graph, conf, sourceDirectory(), basePackage, logger, notifier).render()) return false;
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

	private boolean isDistributed(Configuration.Artifact artifact) {
		String identifier = basePackage + ":ontology";
		if (artifact.distribution() == null) return false;
		List<String> versions = ArtifactoryConnector.versions(artifact.distribution().release(), identifier);
		return versions.contains(artifact.version());
	}

	private File sourceDirectory() {
		return new File(root, "src");
	}

	private void mvn(String goal) throws IOException, MavenInvocationException {
		final File pom = createPom(root, basePackage, conf.artifact().version());
		final InvocationResult result = invoke(pom, goal);
		if (result != null && result.getExitCode() != 0) fail(result);
		else if (result == null) throw new IOException("Failed to publish accessor. Maven HOME not found");
	}

	private void fail(InvocationResult result) throws IOException {
		logger.println(errorStream.toString());
		if (result.getExecutionException() != null)
			throw new IOException("Failed to publish accessor.", result.getExecutionException());
		else throw new IOException("Failed to publish accessor. Exit code: " + result.getExitCode());
	}

	private InvocationResult invoke(File pom, String goal) throws MavenInvocationException {
		List<String> goals = new ArrayList<>();
		goals.add("clean");
		goals.add("install");
		if (!goal.isEmpty()) goals.add(goal);
		InvocationRequest request = new DefaultInvocationRequest().setPomFile(pom).setGoals(goals);
		request.setInputStream(InputStream.nullInputStream());
		Invoker invoker = new DefaultInvoker().setMavenHome(systemProperties.mavenHome);
		log(request);
		config(request, systemProperties.mavenHome);
		return invoker.execute(request);
	}

	private void log(InvocationRequest request) { // TODO: OR check compatibility (invoker -> request, invoker.setErrorHandler is deprecated)
		request.setErrorHandler(logger::println);
//		invoker.setOutputHandler(logger::println);
		request.setOutputHandler(s -> errorStream.append(s).append("\n"));
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
		addSourceDirectories(builder);
		addDependencies(builder);
		final File pomFile = new File(root, "pom.xml");
		Commons.write(pomFile.toPath(), new PomTemplate().render(builder.toFrame()));
		return pomFile;
	}

	private void addDependencies(FrameBuilder builder) {
		builder.add("event", new FrameBuilder().add("version", versions.get("event")));
		if(dependsOnMaster()) builder.add("master", new FrameBuilder().add("version", versions.get("master")));
		if(dependsOnChronos()) builder.add("chronos", new FrameBuilder().add("version", versions.get("chronos")));
	}

	private void addSourceDirectories(FrameBuilder builder) {
		builder.add("sourceDirectory", sourceDirectory().getAbsolutePath());
		for (File sourceDirectory : sourceDirectories)
			if (sourceDirectory.getName().equals("shared"))
				builder.add("sourceDirectory", sourceDirectory.getAbsolutePath());
	}

	private boolean dependsOnMaster() {
		return !graph.datamartList().isEmpty();
	}

	private boolean dependsOnChronos() {
		return graph.datamartList().stream().anyMatch(d -> !d.timelineList().isEmpty() || !d.reelList().isEmpty());
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
}