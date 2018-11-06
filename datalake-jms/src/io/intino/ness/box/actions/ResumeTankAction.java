package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.box.slack.Helper;
import io.intino.ness.core.Datalake;
import io.intino.ness.datalake.TankStarter;

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
		final Datalake.EventStore.Tank tank = Helper.findTank(box.datalake(), this.tank);
		if (tank == null) return "tank not found";
		return execute(tank);
	}

	public String execute(Datalake.EventStore.Tank tank) {
		io.intino.ness.graph.Tank jmsTank = Helper.findTank(box.nessGraph(), tank.qualifiedName());
		new TankStarter(box.busManager(), tank).start();
		tank.active(true);
		jmsTank.active(true);
		tank.save$();
		jmsTank.save$();
		return OK;
	}
}