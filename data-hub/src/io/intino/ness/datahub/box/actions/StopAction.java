package io.intino.ness.datahub.box.actions;

import io.intino.alexandria.logger.Logger;
import io.intino.ness.datahub.box.DataHubBox;


public class StopAction {

	public DataHubBox box;
	public io.intino.alexandria.core.Context context = new io.intino.alexandria.core.Context();

	public void execute() {
		new Thread(() -> {
			box.close();
			Logger.info("Stopping...");
			System.exit(0);
		}).start();
	}
}