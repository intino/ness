package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.box.slack.Helper;
import io.intino.ness.core.Datalake;

import java.time.Instant;

import static org.slf4j.LoggerFactory.getLogger;

public class SortTankAction {

	public NessBox box;
	public String tank;
	public Instant from;

	public SortTankAction() {
		from = null;
	}

	public SortTankAction(NessBox box, Datalake.EventStore.Tank tank, Instant instant) {
		this.box = box;
		this.tank = tank.qualifiedName();
		this.from = instant;
	}

	public void execute() {
		new Thread(this::syncronousExecute).start();
	}

	public void syncronousExecute() {
		getLogger(SortTankAction.class).info("Starting sorting tank " + tank);
		Helper.findTank(box.datalake(), this.tank).sort(from);
		getLogger(SortTankAction.class).info("Finished sorting tank " + tank);
	}
}