package io.intino.ness.box.actions;

import io.intino.alexandria.core.Context;
import io.intino.ness.box.NessBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.slf4j.Logger.ROOT_LOGGER_NAME;

public class StopAction {
	private static Logger logger = LoggerFactory.getLogger(ROOT_LOGGER_NAME);

	public NessBox box;
	public Context context = new Context();

	public void execute() {
		new Thread(() -> {
			box.close();
			logger.info("Stopping...");
			System.exit(0);
		}).start();
	}
}