package io.intino.ness.triton.box.actions;

import io.intino.alexandria.logger.Logger;
import io.intino.ness.triton.box.TritonBox;


public class SealAction {

	public TritonBox box;

	public void execute() {
		Logger.info("Starting seal of tanks");
		new Thread(() -> {
			box.sessionManager().seal();
			Logger.info("Sealing of tanks finished successfully");
		}).start();
	}
}