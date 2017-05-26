package io.intino.ness.box.actions;

import io.intino.ness.graph.Tank;
import io.intino.ness.box.NessBox;
import io.intino.ness.box.slack.Helper;


public class ReflowAction extends Action {

	public NessBox box;
	public String tank;

	public String execute() {
		Tank tank = Helper.findTank(box, this.tank);
		if (tank == null) return "Tank not found";
		box.datalakeManager().reflow(tank);
		return OK;
	}
}