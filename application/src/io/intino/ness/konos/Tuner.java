package io.intino.ness.konos;

import io.intino.ness.DatalakeManager;
import io.intino.ness.bus.BusManager;
import io.intino.ness.datalake.FileStation;
import io.intino.tara.io.Stash;
import io.intino.tara.magritte.Graph;
import io.intino.tara.magritte.stores.InMemoryFileStore;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

class Tuner {

	private NessConfiguration configuration;

	Tuner(NessConfiguration configuration) {
		this.configuration = configuration;
		initLogger();
	}

	Graph initGraph() {
		return Graph.use(store(), io.intino.ness.Ness.class).load("Ness");
	}

	private io.intino.tara.magritte.Store store() {
		return new InMemoryFileStore(configuration.store()) {
			public void writeStash(Stash stash, String path) {
				stash.language = stash.language == null || stash.language.isEmpty() ? "Ness" : stash.language;
				super.writeStash(stash, path);
			}
		};
	}

	void start(NessBox box) {
		BusManager manager = new BusManager(box);
		manager.start();
		box.put("datalake", new DatalakeManager(new FileStation(box.get("ness.rootPath")), manager));
	}

	void terminate(NessBox box) {
		box.get(DatalakeManager.class).quit();

	}

	private static void initLogger() {
		final Logger logger = Logger.getGlobal();
		final ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(Level.INFO);
		handler.setFormatter(new io.intino.konos.LogFormatter("log"));
		logger.addHandler(handler);
	}
}