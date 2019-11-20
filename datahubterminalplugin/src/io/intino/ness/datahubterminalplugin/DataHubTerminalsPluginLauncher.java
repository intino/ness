package io.intino.ness.datahubterminalplugin;

import io.intino.datahub.graph.DataHubTerminal;
import io.intino.datahub.graph.Datalake.Tank;
import io.intino.datahub.graph.NessGraph;
import io.intino.plugin.PluginLauncher;
import io.intino.tara.magritte.Graph;
import io.intino.tara.magritte.stores.FileSystemStore;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class DataHubTerminalsPluginLauncher extends PluginLauncher {
	@Override
	public void run() {
		if (invokedPhase.ordinal() < 2) return;
		logger().println("Building " + configuration().artifact().name$() + " terminal");
		List<File> directories = moduleStructure().resDirectories;
		File resDirectory = directories.stream().filter(d -> {
			File[] files = d.getAbsoluteFile().listFiles(f -> f.getName().endsWith(".stash"));
			return files != null && files.length > 0;
		}).findFirst().orElse(null);
		if (resDirectory == null) return;
		String[] stashes = Arrays.stream(Objects.requireNonNull(resDirectory.listFiles(f -> f.getName().endsWith(".stash")))).map(f -> f.getName().replace(".stash", "")).toArray(String[]::new);
		Graph graph = new Graph(new FileSystemStore(resDirectory)).loadStashes(stashes);
		publishAccessor(graph.as(NessGraph.class));
	}

	private void publishAccessor(NessGraph nessGraph) {
		try {
			File tempDir = Files.createTempDirectory("_temp").toFile();
			AtomicBoolean published = new AtomicBoolean(true);
			nessGraph.dataHubTerminalList().forEach(terminal -> {
				published.set(new TerminalPublisher(new File(tempDir, terminal.name$()), terminal, tanks(terminal), configuration(), systemProperties(), invokedPhase, logger()).publish() & published.get());
//				if (published.get())
//					notifier().notify("MessageHub " + terminal.name$() + " " + participle() + ". Copy maven dependency:\n" + accessorDependency(configuration().artifact().groupId(), terminal.name$(), configuration().artifact().version()));
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

	private List<Tank.Event> tanks(DataHubTerminal messageHub) {
		List<Tank.Event> tanks = new ArrayList<>();
		if (messageHub.publish() != null) tanks.addAll(messageHub.publish().tanks());
		if (messageHub.subscribe() != null) tanks.addAll(messageHub.subscribe().tanks());
		return tanks;
	}
}