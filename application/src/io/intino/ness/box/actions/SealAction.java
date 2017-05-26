package io.intino.ness.box.actions;

import io.intino.ness.graph.Tank;
import io.intino.ness.box.NessBox;

import static io.intino.ness.box.slack.Helper.findTank;


public class SealAction extends Action {

	public NessBox box;
	public String tank;

	public String execute() {
		Tank tank = findTank(box, this.tank);
		box.datalakeManager().seal(tank);
		return OK;
	}


}