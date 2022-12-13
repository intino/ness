package io.intino.ness.datahubterminalplugin;

import io.intino.Configuration;
import io.intino.itrules.FrameBuilder;
import io.intino.plugin.PluginLauncher.SystemProperties;
import org.apache.maven.shared.invoker.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MavenTerminalExecutor {
	private final File root;
	private final String basePackage;
	private final Target target;
	private final String terminalName;
	private final Map<String, String> versions;
	private final Configuration conf;
	private final SystemProperties systemProperties;
	private final PrintStream logger;

	public enum Target {EventsAndEntities, Events, Bpm, Master}

	public MavenTerminalExecutor(File root, String basePackage, Target target, String terminalName, Map<String, String> versions, Configuration conf, SystemProperties systemProperties, PrintStream logger) {
		this.root = root;
		this.basePackage = basePackage;
		this.target = target;
		this.terminalName = terminalName;
		this.versions = versions;
		this.conf = conf;
		this.systemProperties = systemProperties;
		this.logger = logger;
	}

	public void mvn(String goal) throws IOException, MavenInvocationException {
		final File pom = createPom(root, basePackage, terminalName, conf.artifact().version());
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
		invoker.setErrorHandler(logger::println);
//		invoker.setOutputHandler(logger::println);
	}

	private void config(InvocationRequest request, File mavenHome) {
		final File mvn = new File(mavenHome, "bin" + File.separator + "mvn");
		mvn.setExecutable(true);
		request.setJavaHome(systemProperties.javaHome);
	}

	private File createPom(File root, String group, String artifact, String version) {
		final FrameBuilder builder = new FrameBuilder("pom").add("group", group)
				.add("artifact", artifact).add("version", version);
		conf.repositories().forEach(r -> buildRepoFrame(builder, r));
		if (conf.artifact().distribution() != null) {
			if (isSnapshotVersion()) buildDistroFrame(builder, conf.artifact().distribution().snapshot());
			else buildDistroFrame(builder, conf.artifact().distribution().release());
		}
		if (target.equals(Target.Events) || target.equals(Target.EventsAndEntities))
			builder.add("terminal", terminalDependenciesFrame(group, version));
		if (target.equals(Target.Bpm)) builder.add("bpm", versions.get("bpm"));
		final File pomFile = new File(root, "pom.xml");
		Commons.write(pomFile.toPath(), new PomTemplate().render(builder.toFrame()));
		return pomFile;
	}


	private boolean isSnapshotVersion() {
		return conf.artifact().version().contains("SNAPSHOT");
	}

	private FrameBuilder terminalDependenciesFrame(String group, String version) {
		return new FrameBuilder("terminal").
				add("group", group).
				add("artifact", "ontology").
				add("terminalVersion", versions.get("terminal-jms")).
				add("ingestionVersion", versions.get("ingestion")).
				add("masterVersion", versions.get("master")).
				add("version", version);
	}

	private void buildRepoFrame(FrameBuilder builder, Configuration.Repository r) {
		builder.add("repository", createRepositoryFrame(r).toFrame());
	}

	private void buildDistroFrame(FrameBuilder builder, Configuration.Repository r) {
		builder.add("repository", createRepositoryFrame(r).add("distribution").toFrame());
	}

	private FrameBuilder createRepositoryFrame(Configuration.Repository repo) {
		return new FrameBuilder("repository", repo.getClass().getSimpleName()).
				add("name", repo.identifier()).
				add("random", UUID.randomUUID().toString()).
				add("url", repo.url()).
				add("snapshot", repo instanceof Configuration.Repository.Snapshot);
	}
}