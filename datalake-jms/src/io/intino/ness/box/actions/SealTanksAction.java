package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import org.slf4j.LoggerFactory;


public class SealTanksAction {

	public Boolean disableSort = false;
	public NessBox box;

	public void execute() {
		LoggerFactory.getLogger(SortTanksAction.class).info("Starting seal of tanks");
		new Thread(() -> {
			box.datalake().tankList().forEach(tank -> {
				if (!disableSort) tank.sort();
				tank.seal();
			});
			LoggerFactory.getLogger(SortTanksAction.class).info("Sealing of tanks finished successfully");
		}
		).start();
	}
}