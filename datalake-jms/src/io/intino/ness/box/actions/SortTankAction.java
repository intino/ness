package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.box.slack.Helper;
import io.intino.ness.datalake.graph.Tank;

import java.time.Instant;


public class SortTankAction {

	public NessBox box;
	public String tank;
	public Instant from;

	public SortTankAction() {
		from = Instant.MIN;
	}

	public SortTankAction(NessBox box, Tank tank, Instant day) {
		this.box = box;
		this.tank = tank.qualifiedName();
		this.from = day;
	}

	public void execute() {
		Helper.findTank(box.datalake(), this.tank).sort(from);
	}
}