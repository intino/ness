package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.core.Datalake;
import io.intino.ness.datalake.TankStarter;

import static io.intino.ness.box.Utils.findTank;
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
		final Datalake.EventStore.Tank tank = findTank(box.datalake(), this.tank);
		if (tank == null) return "tank not found";
		return execute(tank);
	}

	public String execute(Datalake.EventStore.Tank tank) {
		io.intino.ness.graph.Tank jmsTank = findTank(box.graph(), tank.name());
		new TankStarter(box, tank).start();
		jmsTank.active(true);
		jmsTank.save$();
		return OK;
	}
}