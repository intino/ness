package io.intino.ness.triton.box;

import io.intino.ness.triton.graph.TritonGraph;
import io.intino.tara.io.Stash;
import io.intino.tara.magritte.Graph;
import io.intino.tara.magritte.stores.InMemoryFileStore;

import java.io.File;

public class Main {
	public static void main(String[] args) {
		TritonConfiguration boxConfiguration = new TritonConfiguration(args);
		final TritonBox box = new TritonBox(boxConfiguration);
		Graph graph = new Graph(store(box.storeDirectory())).loadStashes("Ness");
		if (box.configuration.args().containsKey("configurationModel") && !box.configuration.args().get("configurationModel").isEmpty())
			graph.loadStashes(box.configuration.args().get("configurationModel"));
		box.put(graph.as(TritonGraph.class));
		graph.as(TritonGraph.class).datalake().tankList().forEach(t -> box.datalake().eventStore().tank(t.name()));
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