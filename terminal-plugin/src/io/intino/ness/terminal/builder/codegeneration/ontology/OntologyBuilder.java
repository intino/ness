package io.intino.ness.terminal.builder.codegeneration.ontology;

import io.intino.Configuration;
import io.intino.builder.CompilerConfiguration;
import io.intino.datahub.model.NessGraph;
import io.intino.itrules.FrameBuilder;
import io.intino.ness.terminal.builder.ArtifactoryConnector;
import io.intino.ness.terminal.builder.IntinoException;
import io.intino.ness.terminal.builder.codegeneration.Formatters;
import io.intino.ness.terminal.builder.codegeneration.PomTemplate;
import io.intino.ness.terminal.builder.codegeneration.Project;
import io.intino.ness.terminal.builder.util.Version;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.intino.builder.BuildConstants.PRESENTABLE_MESSAGE;


public class OntologyBuilder {
	private final File root;
	private final NessGraph graph;
	private final CompilerConfiguration configuration;
	private final List<File> sourceDirectories;
	private final Map<String, String> versions;
	private final String basePackage;

	public OntologyBuilder(File root, NessGraph graph, CompilerConfiguration configuration, Map<String, String> versions) {
		this.root = root;
		this.graph = graph;
		this.configuration = configuration;
		this.sourceDirectories = List.of(configuration.srcDirectory());
		this.versions = versions;
		this.basePackage = configuration.groupId().toLowerCase() + "." + Formatters.snakeCaseToCamelCase().format(configuration.artifactId()).toString().toLowerCase();
	}

	public Project build() throws IntinoException {
		if (configuration.phase().equals(CompilerConfiguration.Phase.DISTRIBUTE) && !isSnapshotVersion() && isDistributed())
			throw new IntinoException("The Version " + configuration.version() + " is Already Distributed.");
		new OntologyRenderer(graph, configuration, sourceDirectory(), basePackage).render();
		final File pom = createPom(root, basePackage, configuration.version());
		configuration.out().println(PRESENTABLE_MESSAGE + "Terminalc: Ontology created!");
		return new Project(coords(), pom);
	}

	private boolean isDistributed() {
		String identifier = basePackage + ":ontology";
		if (configuration.releaseDistributionRepository() == null) return false;
		List<Version> versions = ArtifactoryConnector.versions(configuration.releaseDistributionRepository(), identifier);
		return versions.stream().anyMatch(v -> v.get().equals(configuration.version()));
	}

	private File sourceDirectory() {
		return new File(root, "src");
	}

	private File createPom(File root, String group, String version) {
		final FrameBuilder builder = new FrameBuilder("pom").add("group", group).add("artifact", "ontology").add("version", version);
		configuration.repositories().forEach(r -> buildRepoFrame(builder, r));
		if (configuration.snapshotDistributionRepository() != null && isSnapshotVersion())
			buildDistroFrame(builder, configuration.snapshotDistributionRepository());
		if (configuration.releaseDistributionRepository() != null && !isSnapshotVersion())
			buildDistroFrame(builder, configuration.releaseDistributionRepository());
		builder.add("sourceDirectory", sourceDirectory().getAbsolutePath());
		for (File sourceDirectory : sourceDirectories)
			if (sourceDirectory.getName().equals("shared"))
				builder.add("sourceDirectory", sourceDirectory.getAbsolutePath());
		builder.add("event", new FrameBuilder().add("version", versions.get("event")));
		builder.add("master", new FrameBuilder().add("masterVersion", versions.get("master")));
		builder.add("led", new FrameBuilder().add("version", versions.get("led")));
		final File pomFile = new File(root, "pom.xml");
		io.intino.ness.terminal.builder.Commons.write(pomFile.toPath(), new PomTemplate().render(builder.toFrame()));
		return pomFile;
	}

	private String coords() {
		return String.join(":", basePackage, "ontology", configuration.version());
	}

	private boolean isSnapshotVersion() {
		return configuration.version().contains("SNAPSHOT");
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