package master.general;

import io.intino.alexandria.core.Box;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.model.NessGraph;
import io.intino.magritte.framework.Graph;

public class MasterServer_ {

	private static final String[] stashes = {"Master, Solution"};

	public static void main(String[] args) {
		NessGraph graph = new Graph().loadStashes(stashes).as(NessGraph.class);
		Box box = new DataHubBox(args()).put(graph.core$()).start();
		Runtime.getRuntime().addShutdownHook(new Thread(box::stop));
	}

	private static String[] args() {
		return new String[] {
				"backup_directory=temp/backup",
				"broker_port=62123",
				"broker_secondary_port=62124",
				"ui_port=62125"
		};
	}
}
