package systems.intino.eventsourcing.datahubbuilder.codegeneration.ontology;

import io.intino.Configuration;
import io.intino.builder.CompilerConfiguration;
import io.intino.itrules.Engine;
import io.intino.itrules.FrameBuilder;
import systems.intino.eventsourcing.datahub.model.NessGraph;
import systems.intino.eventsourcing.datahubbuilder.ArtifactoryConnector;
import systems.intino.eventsourcing.datahubbuilder.IntinoException;
import systems.intino.eventsourcing.datahubbuilder.codegeneration.Commons;
import systems.intino.eventsourcing.datahubbuilder.codegeneration.Formatters;
import systems.intino.eventsourcing.datahubbuilder.codegeneration.PomTemplate;
import systems.intino.eventsourcing.datahubbuilder.codegeneration.Project;
import systems.intino.eventsourcing.datahubbuilder.util.Version;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.intino.builder.BuildConstants.PRESENTABLE_MESSAGE;
import static io.intino.builder.CompilerConfiguration.Phase;


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
		if (configuration.invokedPhase().equals(Phase.DISTRIBUTE) && !isSnapshotVersion() && isDistributed())
			throw new IntinoException("The Version " + configuration.version() + " is Already Distributed.");
		new OntologyRenderer(graph, configuration, sourceDirectory(), basePackage).render();
		final File pom = createPom(root, basePackage, configuration.version());
		configuration.out().println(PRESENTABLE_MESSAGE + "nessc: Ontology created!");
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
		addSourceDirectories(builder);
		addDependencies(builder);
		return renderPom(root, builder);
	}

	private String coords() {
		return String.join(":", basePackage, "ontology", configuration.version());
	}

	private static File renderPom(File root, FrameBuilder builder) {
		final File pomFile = new File(root, "pom.xml");
		Commons.write(pomFile.toPath(), new Engine(new PomTemplate()).render(builder.toFrame()));
		return pomFile;
	}

	private void addDependencies(FrameBuilder builder) {
		builder.add("event", new FrameBuilder().add("version", versions.get("event")));
		builder.add("terminalontology", new FrameBuilder().add("version", versions.get("terminal")));
	}

	private void addSourceDirectories(FrameBuilder builder) {
		builder.add("sourceDirectory", sourceDirectory().getAbsolutePath());
		for (File sourceDirectory : sourceDirectories)
			if (sourceDirectory.getName().equals("shared"))
				builder.add("sourceDirectory", sourceDirectory.getAbsolutePath());
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