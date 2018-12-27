package io.intino.ness.box;

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
		box.put(graph.as(NessGraph.class));
		graph.as(NessGraph.class).tankList().forEach(t -> box.datalake().eventStore().tank(t.name()));
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