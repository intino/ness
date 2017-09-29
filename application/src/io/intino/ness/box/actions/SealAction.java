package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.graph.Tank;

import static io.intino.ness.box.slack.Helper.findTank;


public class SealAction extends Action {

	public NessBox box;
	public String tank;

	public String execute() {
		if (this.tank == null || tank.isEmpty()) {
			for (Tank tank : box.ness().tankList()) seal(tank);
			return OK;
		} else {
			Tank tank = findTank(box, this.tank);
			if (tank == null) return "Tank not found";
			seal(tank);
			return OK;
		}
	}

	private void seal(Tank tank) {
//		box.datalakeManager().seal(tank);
	}
}