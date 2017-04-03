package io.intino.ness.konos;

import io.intino.ness.Ness;
import io.intino.ness.TopicManager;
import io.intino.ness.datalake.filesystem.FileDataLake;
import io.intino.tara.magritte.Graph;
import io.intino.tara.magritte.Store;
import io.intino.tara.magritte.stores.InMemoryFileStore;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

class Setup {

	static Graph initGraph(NessConfiguration configuration) {
		return Graph.use(store(configuration), io.intino.ness.Ness.class).load("ness");
	}

	private static Store store(NessConfiguration configuration) {
		return new InMemoryFileStore(configuration.store());
	}

	static void configureBox(NessBox box) {
		configureLogger("log"); //TODO change path in case
		FileDataLake lake = new FileDataLake(box.get("ness.rootPath"));
		box.put("datalake", lake);
	}

	static void execute(NessBox box) {
		TopicManager topicManager = new TopicManager(box.graph().wrapper(Ness.class), box.topicsBus());
		box.put("topicManager", topicManager);
		topicManager.reload();
	}

	private static void configureLogger(String path) {
		final Logger logger = Logger.getGlobal();
		final ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(Level.INFO);
		handler.setFormatter(new io.intino.konos.LogFormatter(path));
		logger.addHandler(handler);
	}
}