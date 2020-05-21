package io.intino.ness.datahubterminalplugin;

import io.intino.alexandria.logger.Logger;
import io.intino.datahub.graph.Datalake.Tank;
import io.intino.datahub.graph.NessGraph;
import io.intino.datahub.graph.Terminal;
import io.intino.magritte.framework.Graph;
import io.intino.magritte.framework.stores.FileSystemStore;
import io.intino.plugin.PluginLauncher;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class DataHubTerminalsPluginLauncher extends PluginLauncher {
	private static final String MINIMUM_TERMINAL_JMS_VERSION = "3.0.0";
	private static final String MAX_TERMINAL_JMS_VERSION = "4.0.0";
	private static final String MINIMUM_BPM_VERSION = "1.2.5";

	@Override
	public void run() {
		if (invokedPhase.ordinal() < 2) return;
		logger().println("Building " + configuration().artifact().name() + " terminal");
		File tempDir = tempDirectory();
		run(tempDir);
	}

	public void run(File tempDir) {
		List<File> directories = moduleStructure().resDirectories;
		File resDirectory = directories.stream().filter(d -> {
			File[] files = d.getAbsoluteFile().listFiles(f -> f.getName().endsWith(".stash"));
			return files != null && files.length > 0;
		}).findFirst().orElse(null);
		if (resDirectory == null) {
			notifier().notifyError("Stashes not found. Please compile module");
			return;
		}
		String[] stashes = Arrays.stream(Objects.requireNonNull(resDirectory.listFiles(f -> f.getName().endsWith(".stash")))).map(f -> f.getName().replace(".stash", "")).toArray(String[]::new);
		Graph graph = new Graph(new FileSystemStore(resDirectory)).loadStashes(stashes);
		if (graph == null) {
			notifier().notifyError("Couldn't load graph. Please recompile module");
			return;
		}
		publishOntology(graph.as(NessGraph.class), tempDir);
		publishTerminals(graph.as(NessGraph.class), tempDir);
	}

	private void publishOntology(NessGraph graph, File tempDir) {
		try {
			AtomicBoolean published = new AtomicBoolean(true);
			published.set(new OntologyPublisher(new File(tempDir, "ontology"), eventTanks(graph), graph.eventList(), configuration(), systemProperties(), invokedPhase, logger()).publish() & published.get());
			if (published.get() && notifier() != null)
				notifier().notify("Ontology " + participle() + ". Copy maven dependency:\n" + accessorDependency(configuration().artifact().groupId() + "." + Formatters.snakeCaseToCamelCase().format(configuration().artifact().name()).toString().toLowerCase(), "ontology", configuration().artifact().version()));
			if (published.get()) FileUtils.deleteDirectory(tempDir);
		} catch (IOException e) {
			logger().println(e.getMessage());
		}
	}

	private void publishTerminals(NessGraph nessGraph, File tempDir) {
		try {
			String terminalJmsVersion = terminalJmsVersion();
			String bpmVersion = bpmVersion();
			AtomicBoolean published = new AtomicBoolean(true);
			nessGraph.terminalList().forEach(terminal -> {
				published.set(new TerminalPublisher(new File(tempDir, terminal.name$()), terminal, tanks(terminal), configuration(), terminalJmsVersion, bpmVersion, systemProperties(), invokedPhase, logger()).publish() & published.get());
				if (published.get() && notifier() != null)
					notifier().notify("Terminal " + terminal.name$() + " " + participle() + ". Copy maven dependency:\n" + accessorDependency(configuration().artifact().groupId() + "." + Formatters.snakeCaseToCamelCase().format(configuration().artifact().name()).toString().toLowerCase(), terminalNameArtifact(terminal), configuration().artifact().version()));
			});
			if (published.get()) FileUtils.deleteDirectory(tempDir);
		} catch (IOException e) {
			logger().println(e.getMessage());
		}
	}

	private String terminalNameArtifact(Terminal terminal) {
		return Formatters.firstLowerCase(Formatters.camelCaseToSnakeCase().format(terminal.name$()).toString());
	}

	private String terminalJmsVersion() {
		List<String> terminalVersions = ArtifactoryConnector.terminalVersions();
		Collections.reverse(terminalVersions);

		return terminalVersions.isEmpty() ? MINIMUM_TERMINAL_JMS_VERSION : suitableVersion(terminalVersions);
	}

	private String suitableVersion(List<String> terminalVersions) {
		return terminalVersions.stream().filter(version -> version.compareTo(MAX_TERMINAL_JMS_VERSION) < 0).findFirst().orElse(MINIMUM_TERMINAL_JMS_VERSION);
	}

	private String bpmVersion() {
		List<String> bpmVersions = ArtifactoryConnector.bpmVersions();
		return bpmVersions.isEmpty() ? MINIMUM_BPM_VERSION : bpmVersions.get(bpmVersions.size() - 1);
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

	private List<Tank.Event> eventTanks(NessGraph nessGraph) {
		if (nessGraph.datalake() == null) return Collections.emptyList();
		return nessGraph.datalake().tankList().stream().filter(Tank::isEvent).map(Tank::asEvent).collect(Collectors.toList());
	}

	private List<Tank.Event> tanks(Terminal terminal) {
		List<Tank.Event> tanks = new ArrayList<>();
		if (terminal.publish() != null) tanks.addAll(terminal.publish().tanks());
		if (terminal.subscribe() != null) tanks.addAll(terminal.subscribe().tanks());
		return tanks;
	}
}