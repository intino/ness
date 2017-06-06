package io.intino.ness.box;

import io.intino.konos.Box;
import io.intino.tara.io.Stash;
import io.intino.tara.magritte.Graph;
import io.intino.tara.magritte.stores.InMemoryFileStore;

import java.io.File;

public class Main {
	public static void main(String[] args) {
		NessConfiguration boxConfiguration = new NessConfiguration(args);
		Graph graph = new Graph(store(boxConfiguration.args().get("ness_store"))).loadStashes("Ness");
		Box box = new NessBox(boxConfiguration).put(graph).open();
		Runtime.getRuntime().addShutdownHook(new Thread(box::close));
	}

	private static io.intino.tara.magritte.Store store(String directory) {
		return new InMemoryFileStore(new File(directory)) {
			public void writeStash(Stash stash, String path) {
				stash.language = stash.language == null || stash.language.isEmpty() ? "Ness" : stash.language;
				super.writeStash(stash, path);
			}
		};
	}
}