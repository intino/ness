package io.intino.ness.box.actions;

import io.intino.alexandria.core.Context;
import io.intino.alexandria.logger.Logger;
import io.intino.ness.box.NessServiceBox;

public class StopAction {

	public NessServiceBox box;
	public Context context = new Context();

	public void execute() {
		new Thread(() -> {
			box.close();
			Logger.info("Stopping...");
			System.exit(0);
		}).start();
	}
}