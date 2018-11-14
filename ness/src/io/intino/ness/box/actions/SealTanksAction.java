package io.intino.ness.box.actions;

import io.intino.alexandria.logger.Logger;
import io.intino.ness.box.NessBox;


public class SealTanksAction {

	public Boolean disableSort = false;
	public NessBox box;

	public void execute() {
		Logger.info("Starting seal of tanks");
		new Thread(() -> {
			box.datalake().seal();
			Logger.info("Sealing of tanks finished successfully");
		}).start();
	}
}