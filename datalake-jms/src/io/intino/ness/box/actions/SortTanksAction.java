package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.datalake.graph.Tank;
import org.slf4j.LoggerFactory;

import static org.slf4j.LoggerFactory.getLogger;

public class SortTanksAction {
	public NessBox box;

	public void execute() {
		getLogger(SortTanksAction.class).info("Starting tanks sorting");
		new Thread(() -> {
			box.datalake().tankList().forEach(Tank::sort);
			getLogger(SortTanksAction.class).info("Sorting of tanks finished successfully");
		}).start();
	}
}