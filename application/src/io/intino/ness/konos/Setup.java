package io.intino.ness.konos;

import io.intino.ness.datalake.filesystem.FileDataLake;
import io.intino.tara.magritte.Graph;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

class Setup {

	static Graph initGraph(ApplicationConfiguration configuration) {
		return Graph.use(store(configuration), io.intino.ness.Application.class).load("application");
	}

	private static io.intino.tara.magritte.Store store(ApplicationConfiguration configuration) {
		return new io.intino.tara.magritte.stores.FileSystemStore(configuration.store());
	}

	static void configureBox(ApplicationBox box) {
		configureLogger("log"); //TODO change path in case
		FileDataLake lake = new FileDataLake(box.get("ness.rootPath"));
		box.put("datalake", lake);
	}
	private static void configureLogger(String path) {
    		final Logger logger = Logger.getGlobal();
		final ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(Level.INFO);
		handler.setFormatter(new io.intino.konos.LogFormatter(path));
		logger.addHandler(handler);
    	}
}