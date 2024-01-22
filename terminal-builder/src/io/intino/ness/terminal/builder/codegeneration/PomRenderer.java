package io.intino.ness.terminal.builder.codegeneration;

import io.intino.Configuration.Repository;
import io.intino.datahub.model.Terminal;
import io.intino.itrules.FrameBuilder;
import io.intino.ness.terminal.builder.IntinoException;
import io.intino.plugin.CompilerConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class PomRenderer {
	private final CompilerConfiguration configuration;
	private final Map<String, String> versions;
	private final File root;
	private final String groupId;

	public enum Target {Events, Bpm;}

	public PomRenderer(CompilerConfiguration configuration, Map<String, String> versions, File root, String groupId) {
		this.configuration = configuration;
		this.versions = versions;
		this.root = root;
		this.groupId = groupId;
	}

	public File render(Terminal terminal) throws IntinoException {
		ArrayList<Target> targets = targets(terminal);
		final FrameBuilder builder = new FrameBuilder("pom").add("group", groupId)
				.add("artifact", terminalNameArtifact(terminal)).add("version", configuration.version());
		for (Repository r : configuration.repositories()) buildRepoFrame(builder, r);
		if (configuration.releaseDistributionRepository() != null || configuration.snapshotDistributionRepository() != null) {
			if (isSnapshotVersion()) buildDistroFrame(builder, configuration.snapshotDistributionRepository());
			else buildDistroFrame(builder, configuration.releaseDistributionRepository());
		}
		if (targets.contains(Target.Events))
			builder.add("terminal", terminalDependenciesFrame(groupId, configuration.version()));
		if (targets.contains(Target.Bpm)) builder.add("bpm", versions.get("bpm"));
		final File pomFile = new File(root, "pom.xml");
		Commons.write(pomFile.toPath(), new PomTemplate().render(builder.toFrame()));
		return pomFile;
	}

	public String coors(Terminal terminal) {
		return String.join(":", groupId, terminalNameArtifact(terminal), configuration.version());
	}

	private ArrayList<Target> targets(Terminal terminal) {
		ArrayList<Target> targets = new ArrayList<>();
		targets.add(Target.Events);
		if (terminal.bpm() != null) targets.add(Target.Bpm);
		return targets;
	}

	private String terminalNameArtifact(Terminal terminal) {
		return Formatters.firstLowerCase(Formatters.camelCaseToSnakeCase().format(terminal.name$()).toString());
	}

	private boolean isSnapshotVersion() {
		return configuration.version().contains("SNAPSHOT");
	}

	private FrameBuilder terminalDependenciesFrame(String group, String version) {
		return new FrameBuilder("terminal").
				add("group", group).
				add("artifact", "ontology").
				add("terminalVersion", versions.get("terminal-jms")).
				add("ingestionVersion", versions.get("ingestion")).
				add("datalakeVersion", versions.get("datalake")).
				add("version", version);
	}

	private void buildRepoFrame(FrameBuilder builder, Repository r) throws IntinoException {
		if (r == null) throw new IntinoException("Repository not found");
		builder.add("repository", createRepositoryFrame(r).toFrame());
	}

	private void buildDistroFrame(FrameBuilder builder, Repository r) throws IntinoException {
		if (r == null) throw new IntinoException("Repository not found");
		builder.add("repository", createRepositoryFrame(r).add("distribution").toFrame());
	}

	private FrameBuilder createRepositoryFrame(Repository repo) {
		return new FrameBuilder("repository", repo.getClass().getSimpleName()).
				add("name", repo.identifier()).
				add("random", UUID.randomUUID().toString()).
				add("url", repo.url()).
				add("snapshot", repo instanceof Repository.Snapshot);
	}
}
