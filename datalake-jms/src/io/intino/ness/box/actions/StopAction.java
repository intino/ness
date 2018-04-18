package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.slf4j.Logger.ROOT_LOGGER_NAME;

public class StopAction {
	private static Logger logger = LoggerFactory.getLogger(ROOT_LOGGER_NAME);

	public NessBox box;
	public io.intino.konos.alexandria.schema.Context context = new io.intino.konos.alexandria.schema.Context();

	public void execute() {
		new Thread(() -> {
			box.close();
			logger.info("Stopping...");
			System.exit(0);
		}).start();
	}
}