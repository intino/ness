package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.graph.Tank;

import static io.intino.ness.box.slack.Helper.findTank;


public class SealAction extends Action {

	public NessBox box;
	public String tank;

	public String execute() {
		return OK;
	}

//	private void seal(Tank tank) {
//		box.datalakeManager().seal(tank);
//	}
}