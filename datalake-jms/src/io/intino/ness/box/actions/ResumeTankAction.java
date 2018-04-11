package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.box.slack.Helper;
import io.intino.ness.datalake.TankStarter;
import io.intino.ness.datalake.graph.Tank;

import static io.intino.ness.box.actions.Action.OK;


public class ResumeTankAction {
	public NessBox box;
	public String tank;

	public ResumeTankAction() {
	}

	public ResumeTankAction(NessBox box, String tank) {
		this.box = box;
		this.tank = tank;
	}

	public String execute() {
		final Tank tank = Helper.findTank(box.datalake(), this.tank);
		if (tank == null) return "tank not found";
		return execute(tank);
	}

	public String execute(Tank tank) {
		new TankStarter(box.busManager(), tank).start();
		tank.active(true);
		tank.save$();
		return OK;
	}
}