package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.box.slack.Helper;
import io.intino.ness.graph.Tank;

import java.time.Instant;


public class SortTankAction {

	public NessBox box;
	public String tank;
	public Instant day;

	public SortTankAction() {

	}

	public SortTankAction(NessBox box, Tank tank, Instant day) {
		this.box = box;
		this.tank = tank.qualifiedName();
		this.day = day;
	}

	public void execute() {
		box.datalakeManager().sort(Helper.findTank(box, tank), day);
	}
}