package org.example.test.model;

import io.intino.alexandria.core.Box;
import io.intino.alexandria.logger.Logger;
import org.apache.log4j.Level;
import org.example.test.ness.GraphLoader;
import systems.intino.eventsourcing.datahub.box.DatahubBox;
import systems.intino.eventsourcing.datahub.box.DatahubConfiguration;
import systems.intino.eventsourcing.datahub.model.NessGraph;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static io.intino.alexandria.logger4j.Logger.setLevel;

public class Main {

	public static void main(String[] args) {
		DatahubConfiguration configuration = new DatahubConfiguration(args);
		NessGraph graph = GraphLoader.load();
		loadUsers(configuration.home(), graph);
		Box box = new DatahubBox(args).put(graph.core$());
		setLevel(Level.ERROR);
		box.start();
		Runtime.getRuntime().addShutdownHook(new Thread(box::stop));
	}

	private static void loadUsers(File workspace, NessGraph nessGraph) {
		try {
			File file = new File(workspace, "datahub/config/users.bin");
			if (!file.exists()) return;
			nessGraph.broker().clear().user(u -> true);
			String[] users = new String(Files.readAllBytes(file.toPath())).split("\n");
			for (String user : users) nessGraph.broker().create().user(user.split("::")[0], user.split("::")[1]);
		} catch (IOException e) {
			Logger.error(e);
		}
	}
}