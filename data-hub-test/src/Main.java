import io.intino.ness.datahub.box.DataHubBox;
import io.intino.ness.datahub.box.DataHubConfiguration;
import io.intino.ness.datahub.graph.NessGraph;
import io.intino.tara.magritte.Graph;

public class Main {
	public static void main(String[] args) {
		final DataHubBox box = new DataHubBox(new DataHubConfiguration(args));
		Graph graph = new Graph().loadStashes("Ness");
		if (box.configuration.args().containsKey("configurationModel") && !box.configuration.args().get("configurationModel").isEmpty())
			graph.loadStashes(box.configuration.args().get("configurationModel"));
		box.put(graph.as(NessGraph.class)).open();
		Runtime.getRuntime().addShutdownHook(new Thread(box::close));
	}

}