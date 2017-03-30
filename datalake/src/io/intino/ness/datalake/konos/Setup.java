package io.intino.ness.datalake.konos;


import io.intino.ness.datalake.filesystem.FileDataLake;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

class Setup {

	static void configureBox(DatalakeBox box) {
		configureLogger("log"); //TODO change path in case
		FileDataLake fileDataLake = new FileDataLake(box.get("ness.rootPath"));
		box.put("datalake", fileDataLake);
	}

	private static void configureLogger(String path) {
		final Logger logger = Logger.getGlobal();
		final ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(Level.INFO);
		handler.setFormatter(new io.intino.konos.LogFormatter(path));
		logger.addHandler(handler);
	}
}