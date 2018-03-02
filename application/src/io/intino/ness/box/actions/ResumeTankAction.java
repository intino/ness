package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.box.slack.Helper;
import io.intino.ness.datalake.TankStarter;
import io.intino.ness.graph.Tank;

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
		return execute(Helper.findTank(box, tank));
	}

	private String execute(Tank aTank) {
		if (aTank == null) return "tank not found";
		new TankStarter(box.busManager(), box.datalakeManager(), aTank).start();
		aTank.running(true);
		aTank.save$();
		return OK;
	}
}