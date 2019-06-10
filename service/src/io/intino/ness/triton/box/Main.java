package io.intino.ness.triton.box;

import io.intino.ness.triton.graph.TritonGraph;
import io.intino.tara.io.Stash;
import io.intino.tara.magritte.Graph;
import io.intino.tara.magritte.stores.InMemoryFileStore;

import java.io.File;

public class Main {
	public static void main(String[] args) {
		final ServiceBox box = new ServiceBox(new ServiceConfiguration(args));
		Graph graph = new Graph(store(box.storeDirectory())).loadStashes("Triton");
		if (box.configuration.args().containsKey("configurationModel") && !box.configuration.args().get("configurationModel").isEmpty())
			graph.loadStashes(box.configuration.args().get("configurationModel"));
		box.put(graph.as(TritonGraph.class)).open();
		Runtime.getRuntime().addShutdownHook(new Thread(box::close));
	}

	private static io.intino.tara.magritte.Store store(String directory) {
		return new InMemoryFileStore(new File(directory)){
			@Override
			public void writeStash(Stash stash, String path) {
				stash.language = "Triton";
				super.writeStash(stash, path);
			}
		};
	}
}