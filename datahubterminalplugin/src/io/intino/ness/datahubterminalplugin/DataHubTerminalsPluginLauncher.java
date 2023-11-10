package io.intino.ness.datahubterminalplugin;

import io.intino.alexandria.logger.Logger;
import io.intino.datahub.model.NessGraph;
import io.intino.datahub.model.Terminal;
import io.intino.magritte.framework.Graph;
import io.intino.magritte.framework.stores.FileSystemStore;
import io.intino.ness.datahubterminalplugin.ontology.OntologyPublisher;
import io.intino.ness.datahubterminalplugin.terminal.TerminalPublisher;
import io.intino.plugin.PluginLauncher;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

public class DataHubTerminalsPluginLauncher extends PluginLauncher {
	private static final String MINIMUM_CHRONOS_VERSION = "2.0.0";
	private static final String MAX_CHRONOS_VERSION = "3.0.0";
	private static final String MINIMUM_BPM_VERSION = "3.1.1";
	private static final String MAX_BPM_VERSION = "4.0.0";
	private static final String MINIMUM_TERMINAL_JMS_VERSION = "5.4.0";
	private static final String MAX_TERMINAL_JMS_VERSION = "6.0.0";
	private static final String MINIMUM_INGESTION_VERSION = "5.0.2";
	private static final String MAX_INGESTION_VERSION = "6.0.0";
	private static final String MINIMUM_MASTER_VERSION = "2.1.0";
	private static final String MAX_MASTER_VERSION = "3.0.0";
	private static final String MINIMUM_DATALAKE_VERSION = "7.0.2";
	private static final String MAX_DATALAKE_VERSION = "8.0.0";
	private static final String MINIMUM_EVENT_VERSION = "5.0.2";
	private static final String MAX_EVENT_VERSION = "6.0.0";
	private boolean deleteTempDirOnPublish = true;
	private boolean publishTerminalsIfOntologyFails = false;

	@Override
	public void run() {
		if (invokedPhase.ordinal() < 2) return;
		logger().println("Building " + configuration().artifact().name() + " terminals...");
		File tempDir = tempDirectory();
		run(tempDir);
	}

	public void run(File tempDir) {
		if (logger() != null) logger().println("Maven HOME: " + systemProperties.mavenHome.getAbsolutePath());
		File resDirectory = resDirectory(moduleStructure().resDirectories);
		if (resDirectory == null) return;
		NessGraph graph = loadGraph(resDirectory);
		if (hasErrors(graph)) return;
		Map<String, String> versions = versions();
		if (!publishOntology(graph, versions, tempDir) && !publishTerminalsIfOntologyFails) return;
		publishTerminals(graph, versions, tempDir);
		logger().println("Finished generation of terminals!");
	}

	private Map<String, String> versions() {
		return Map.of("terminal-jms", terminalJmsVersion(),
				"ingestion", ingestionVersion(),
				"bpm", bpmVersion(),
				"master", masterVersion(),
				"event", eventVersion(),
				"datalake", datalakeVersion(),
				"chronos", chronosVersion()
		);
	}

	private boolean publishOntology(NessGraph graph, Map<String, String> versions, File tempDir) {
		try {
			AtomicBoolean published = new AtomicBoolean(true);
			published.set(new OntologyPublisher(new File(tempDir, "ontology"), graph, configuration(), moduleStructure(), versions, systemProperties(), invokedPhase, logger(), notifier()).publish() & published.get());
			if (published.get() && notifier() != null)
				notifier().notify("Ontology " + participle() + ". Copy maven dependency:\n" + accessorDependency(configuration().artifact().groupId() + "." + Formatters.snakeCaseToCamelCase().format(configuration().artifact().name()).toString().toLowerCase(), "ontology", configuration().artifact().version()));
			handleTempDir(tempDir, published);
			return published.get();
		} catch (Throwable e) {
			logger().println(e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	private void publishTerminals(NessGraph nessGraph, Map<String, String> versions, File tempDir) {
		try {
			AtomicBoolean published = new AtomicBoolean(true);
			nessGraph.terminalList().parallelStream().forEach(terminal -> {
				published.set(new TerminalPublisher(new File(tempDir, terminal.name$()), terminal, configuration(), versions, systemProperties(), invokedPhase, logger(), notifier()).publish() & published.get());
				if (published.get() && notifier() != null)
					notifier().notify("Terminal " + terminal.name$() + " " + participle() + ". Copy maven dependency:\n" + accessorDependency(configuration().artifact().groupId() + "." + Formatters.snakeCaseToCamelCase().format(configuration().artifact().name()).toString().toLowerCase(), terminalNameArtifact(terminal), configuration().artifact().version()));
			});
			handleTempDir(tempDir, published);
		} catch (Throwable e) {
			logger().println(e.getMessage());
			e.printStackTrace();
		}
	}

	private void handleTempDir(File tempDir, AtomicBoolean published) throws IOException {
		if (published.get() && deleteTempDirOnPublish) FileUtils.deleteDirectory(tempDir);
	}

	private static NessGraph loadGraph(File resDirectory) {
		String[] stashes = Arrays.stream(Objects.requireNonNull(resDirectory.listFiles(f -> f.getName().endsWith(".stash")))).map(f -> f.getName().replace(".stash", "")).toArray(String[]::new);
		final NessGraph graph = new Graph(new FileSystemStore(resDirectory)).loadStashes(stashes).as(NessGraph.class);
		if (graph.messageList(t -> t.name$().equals("Session")).findAny().isEmpty())
			graph.create("Session", "Session").message();
		return graph;
	}

	private boolean hasErrors(NessGraph graph) {
		if (graph == null) {
			notifier().notifyError("Couldn't load graph. Please recompile module");
			return true;
		}
		if (safe(() -> configuration().artifact().distribution()) != null && safe(() -> configuration().artifact().distribution().snapshot()) == null && isSnapshotVersion()) {
			notifier().notifyError("Snapshot distribution repository not found");
			return true;
		}
		return false;
	}

	private File resDirectory(List<File> directories) {
		File resDirectory = directories.stream().filter(d -> {
			File[] files = d.getAbsoluteFile().listFiles(f -> f.getName().endsWith(".stash"));
			return files != null && files.length > 0;
		}).findFirst().orElse(null);
		if (resDirectory == null) {
			notifier().notifyError("Stashes not found. Please compile module");
			return null;
		}
		return resDirectory;
	}

	private String terminalNameArtifact(Terminal terminal) {
		return Formatters.firstLowerCase(Formatters.camelCaseToSnakeCase().format(terminal.name$()).toString());
	}

	private String terminalJmsVersion() {
		return suitableVersion(ArtifactoryConnector.terminalVersions(), MINIMUM_TERMINAL_JMS_VERSION, MAX_TERMINAL_JMS_VERSION);
	}

	private String masterVersion() {
		return suitableVersion(ArtifactoryConnector.masterVersions(), MINIMUM_MASTER_VERSION, MAX_MASTER_VERSION);
	}

	private String ingestionVersion() {
		return suitableVersion(ArtifactoryConnector.ingestionVersions(), MINIMUM_INGESTION_VERSION, MAX_INGESTION_VERSION);
	}

	private String eventVersion() {
		return suitableVersion(ArtifactoryConnector.eventVersions(), MINIMUM_EVENT_VERSION, MAX_EVENT_VERSION);
	}

	private String datalakeVersion() {
		return suitableVersion(ArtifactoryConnector.datalakeVersions(), MINIMUM_DATALAKE_VERSION, MAX_DATALAKE_VERSION);
	}

	private String chronosVersion() {
		return suitableVersion(ArtifactoryConnector.chronosVersions(), MINIMUM_CHRONOS_VERSION, MAX_CHRONOS_VERSION);
	}

	private String bpmVersion() {
		return suitableVersion(ArtifactoryConnector.bpmVersions(), MINIMUM_BPM_VERSION, MAX_BPM_VERSION);
	}

	private String suitableVersion(List<Version> versions, String min, String max) {
		try {
			Version maxVersion = new Version(max);
			Version minVersion = new Version(min);
			return versions.stream().sorted(Comparator.reverseOrder()).filter(v -> v.compareTo(maxVersion) < 0).findFirst().orElse(minVersion).get();
		} catch (IntinoException e) {
			Logger.error(e);
			return min;
		}
	}

	private boolean isSnapshotVersion() {
		return configuration().artifact().version().contains("SNAPSHOT");
	}

	private String participle() {
		return invokedPhase == Phase.INSTALL ? "installed" : "distributed";
	}

	private String accessorDependency(String groupId, String artifactId, String version) {
		return "<dependency>\n" +
				"    <groupId>" + groupId.toLowerCase() + "</groupId>\n" +
				"    <artifactId>" + artifactId.toLowerCase() + "</artifactId>\n" +
				"    <version>" + version + "</version>\n" +
				"</dependency>";
	}

	private File tempDirectory() {
		try {
			return Files.createTempDirectory("_temp").toFile();
		} catch (IOException e) {
			Logger.error(e);
			return new File("");
		}
	}

	public static <T> T safe(Safe.Wrapper<T> wrapper) {
		try {
			return wrapper.value();
		} catch (Throwable var2) {
			return null;
		}
	}

	public void deleteTempDirOnPublish(boolean deleteTempDirOnPublish) {
		this.deleteTempDirOnPublish = deleteTempDirOnPublish;
	}

	public void publishTerminalsIfOntologyFails(boolean publishTerminalsIfOntologyFails) {
		this.publishTerminalsIfOntologyFails = publishTerminalsIfOntologyFails;
	}
}