package io.intino.ness.box;

import io.intino.ness.datalake.graph.DatalakeGraph;
import io.intino.ness.graph.NessGraph;
import io.intino.tara.io.Stash;
import io.intino.tara.magritte.Graph;
import io.intino.tara.magritte.stores.InMemoryFileStore;

import java.io.File;

public class Main {
	public static void main(String[] args) {
		NessConfiguration boxConfiguration = new NessConfiguration(args);
		final NessBox box = new NessBox(boxConfiguration);
		Graph graph = new Graph(store(box.storeDirectory())).loadStashes("Ness");
		if (box.configuration.args().containsKey("configurationModel") && !box.configuration.args().get("configurationModel").isEmpty())
			graph.loadStashes(box.configuration.args().get("configurationModel"));
		final DatalakeGraph datalakeGraph = new Graph().loadStashes("Datalake").as(DatalakeGraph.class);
		box.put(graph.as(NessGraph.class)).put(datalakeGraph);
		graph.as(NessGraph.class).tankList().forEach(t -> datalakeGraph.add(t.qualifiedName()).active(t.active()));
		datalakeGraph.core$().save("tanks");
		box.open();
		Runtime.getRuntime().addShutdownHook(new Thread(box::close));
	}

	private static io.intino.tara.magritte.Store store(String directory) {
		return new InMemoryFileStore(new File(directory)){
			@Override
			public void writeStash(Stash stash, String path) {
				stash.language = "Ness";
				super.writeStash(stash, path);
			}
		};
	}
}