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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class DataHubTerminalsPluginLauncher extends PluginLauncher {
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
		if(graph== null){
			notifier().notifyError("Couldnt load graph. Please recompile module");
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
			AtomicBoolean published = new AtomicBoolean(true);
			nessGraph.terminalList().forEach(terminal -> {
				published.set(new TerminalPublisher(new File(tempDir, terminal.name$()), terminal, tanks(terminal), configuration(), systemProperties(), invokedPhase, logger()).publish() & published.get());
				if (published.get() && notifier() != null)
					notifier().notify("Terminal " + terminal.name$() + " " + participle() + ". Copy maven dependency:\n" + accessorDependency(configuration().artifact().groupId() + "." + Formatters.snakeCaseToCamelCase().format(configuration().artifact().name()).toString().toLowerCase(), terminal.name$(), configuration().artifact().version()));
			});
			if (published.get()) FileUtils.deleteDirectory(tempDir);
		} catch (IOException e) {
			logger().println(e.getMessage());
		}
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
		return nessGraph.datalake().tankList().stream().filter(Tank::isEvent).map(Tank::asEvent).collect(Collectors.toList());
	}

	private List<Tank.Event> tanks(Terminal terminal) {
		List<Tank.Event> tanks = new ArrayList<>();
		if (terminal.publish() != null) tanks.addAll(terminal.publish().tanks());
		if (terminal.subscribe() != null) tanks.addAll(terminal.subscribe().tanks());
		return tanks;
	}
}