package io.intino.ness.triton.box.actions;

import io.intino.ness.triton.box.TritonBox;
import io.intino.ness.triton.datalake.TankWriter;
import io.intino.ness.triton.graph.Datalake;

import static io.intino.ness.triton.box.Utils.findTank;

public class ResumeTankAction {
	public TritonBox box;
	public String tank;

	public ResumeTankAction() {
	}

	public ResumeTankAction(TritonBox box, String tank) {
		this.box = box;
		this.tank = tank;
	}

	public String execute() {
		final Datalake.EventStore.Tank tank = findTank(box.datalake(), this.tank);
		if (tank == null) return "tank not found";
		return execute(tank);
	}

	public String execute(Datalake.EventStore.Tank tank) {
		Datalake.Tank jmsTank = findTank(box.graph(), tank.name());
		new TankWriter(box, tank).register();
		jmsTank.active(true);
		jmsTank.save$();
		return Action.OK;
	}
}