package master.general;

import io.intino.alexandria.core.Box;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.box.DataHubConfiguration;
import io.intino.datahub.model.NessGraph;
import io.intino.magritte.framework.Graph;

import java.io.File;

public class MasterServer_ {

	private static final String[] stashes = {"Events", "Solution", "Entities"};

	public static void main(String[] args) {
		DataHubConfiguration configuration = new DataHubConfiguration(args());
		NessGraph graph = new Graph().loadStashes(stashes).as(NessGraph.class);
		loadUsers(configuration.home(), graph);
		Box box = new DataHubBox(configuration).put(graph.core$()).start();
		Runtime.getRuntime().addShutdownHook(new Thread(box::stop));
	}

	private static void loadUsers(File workspace, NessGraph nessGraph) {
		nessGraph.broker().create().user("test", "test");
	}

	private static String[] args() {
		return new String[] {
				"home=temp",
				"backup_directory=temp/backup",
				"broker_port=62123",
				"broker_secondary_port=62124",
				"ui_port=62125",
				"datalake_path=temp/datalake"
		};
	}
}
