package org.example.test;

import io.intino.alexandria.core.Box;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.box.DataHubConfiguration;
import io.intino.datahub.model.NessGraph;
import io.intino.magritte.framework.Graph;

import java.io.File;

public class Server {

	private static final String[] stashes = {"Solution"};

	public static void main(String[] args) {
		DataHubConfiguration conf = new DataHubConfiguration(arguments());
		NessGraph graph = new Graph().loadStashes(stashes).as(NessGraph.class);
		loadUsers(conf.home(), graph);
		Box box = new DataHubBox(conf).put(graph.core$()).start();
		Runtime.getRuntime().addShutdownHook(new Thread(box::stop));
	}

	private static void loadUsers(File workspace, NessGraph nessGraph) {
		try {
			nessGraph.broker().create().user("test", "test");
			nessGraph.broker().create().user("test2", "test2");
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	private static String[] arguments() {
		return new String[] {
				"home=./temp/",
				"datalake_directory=./temp/datalake",
				"broker_port=63000",
				"broker_secondary_port=1882",
				"backup_directory=./temp/backup",
				"ui_port=9020"
		};
	}
}
