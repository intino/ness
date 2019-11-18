package io.intino.ness.datahubaccessorplugin;

import io.intino.datahub.graph.Datalake.Tank;
import io.intino.datahub.graph.MessageHub;
import io.intino.datahub.graph.NessGraph;
import io.intino.plugin.PluginLauncher;
import io.intino.tara.magritte.Graph;
import io.intino.tara.magritte.stores.FileSystemStore;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class DataHubAccessorsPluginLauncher extends PluginLauncher {
	@Override
	public void run() {
		logger().println("Building " + configuration().artifact().name$() + " accessor");
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
			nessGraph.messageHubList().forEach(messageHub ->
					new AccessorsPublisher(new File(tempDir, messageHub.name$()), messageHub, tanks(messageHub), configuration(), systemProperties(), logger()).publish());
		} catch (IOException e) {
			log.println(e.getMessage());
		}
	}

	private List<Tank.Event> tanks(MessageHub messageHub) {
		List<Tank.Event> tanks = new ArrayList<>();
		if (messageHub.publish() != null) tanks.addAll(messageHub.publish().tanks());
		if (messageHub.subscribe() != null) tanks.addAll(messageHub.subscribe().tanks());
		return tanks;
	}
}