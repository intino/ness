package io.intino.ness.konos;

import io.intino.ness.bus.BusManager;
import io.intino.ness.datalake.FileDataLake;
import io.intino.tara.io.Stash;
import io.intino.tara.magritte.Graph;
import io.intino.tara.magritte.Store;
import io.intino.tara.magritte.stores.InMemoryFileStore;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

final class Setup {

	static Graph initGraph(NessConfiguration configuration) {
		return Graph.use(store(configuration), io.intino.ness.Ness.class).load("Ness");
	}

	private static Store store(NessConfiguration configuration) {
		return new InMemoryFileStore(configuration.store()) {
			public void writeStash(Stash stash, String path) {
				stash.language = stash.language == null || stash.language.isEmpty() ? "Ness" : stash.language;
				super.writeStash(stash, path);
			}
		};
	}

	static void configureBox(NessBox box) {
		configureLogger("log"); //TODO change path pipe case
		FileDataLake lake = new FileDataLake(box.get("ness.rootPath"));
		box.put("datalake", lake);
	}

	static void execute(NessBox box) {
		BusManager manager = new BusManager(box);
		box.put("bus", manager);
		manager.start();
	}

	private static void configureLogger(String path) {
		final Logger logger = Logger.getGlobal();
		final ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(Level.INFO);
		handler.setFormatter(new io.intino.konos.LogFormatter(path));
		logger.addHandler(handler);
	}
}