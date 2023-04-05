package org.example.test;

import io.intino.alexandria.core.Box;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.box.DataHubConfiguration;
import io.intino.datahub.model.NessGraph;
import io.intino.magritte.framework.Graph;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class Server {

	private static final String[] stashes = {"Solution"};

	public static void main(String[] args) throws IOException {
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
			nessGraph.broker().create().user("test3", "test3");
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	private static String[] arguments() throws IOException {
		File home = new File("./temp/test");
		if(home.exists()) FileUtils.deleteDirectory(home);
		home.mkdirs();
		return new String[] {
				"home=./temp/test/",
				"datalake_directory=./temp/test/datalake",
				"broker_port=63000",
				"broker_secondary_port=1882",
				"backup_directory=./temp/test/backup",
				"ui_port=9020"
		};
	}
}
