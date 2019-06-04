package io.intino.ness.triton.box.actions;

import io.intino.ness.datalake.Datalake.EventStore.Tank;
import io.intino.ness.triton.box.TritonBox;
import io.intino.ness.triton.datalake.TankWriter;

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
		final Tank tank = findTank(box.datalake(), this.tank);
		if (tank == null) return "tank not found";
		return execute(tank);
	}

	public String execute(Tank tank) {
		io.intino.ness.triton.graph.Tank graphTank = findTank(box.graph(), tank.name());
		new TankWriter(box, tank).register();
		graphTank.active(true);
		graphTank.save$();
		return Action.OK;
	}
}