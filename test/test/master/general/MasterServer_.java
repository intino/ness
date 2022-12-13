package master.general;

import io.intino.alexandria.core.Box;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.box.DataHubConfiguration;
import io.intino.datahub.model.NessGraph;
import io.intino.magritte.framework.Graph;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class MasterServer_ {

	private static final String[] stashes = {"Master", "Solution"};

	public static void main(String[] args) {
		DataHubConfiguration configuration = new DataHubConfiguration(args());
		NessGraph graph = new Graph().loadStashes(stashes).as(NessGraph.class);
		loadUsers(configuration.home(), graph);
		Box box = new DataHubBox(configuration).put(graph.core$()).start();
		Runtime.getRuntime().addShutdownHook(new Thread(box::stop));
	}

	private static void loadUsers(File workspace, NessGraph nessGraph) {
		try {
			File file = new File(workspace, "datahub/users.bin");
			if (!file.exists()) return;
			nessGraph.broker().clear().user(u -> true);
			String[] users = new String(Files.readAllBytes(file.toPath())).split("\n");
			for (String user : users) nessGraph.broker().create().user(user.split("::")[0], user.split("::")[1]);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private static String[] args() {
		return new String[] {
				"backup_directory=temp/backup",
				"broker_port=62123",
				"broker_secondary_port=62124",
				"ui_port=62125",
				"datalake_path=temp/datalake"
		};
	}
}
