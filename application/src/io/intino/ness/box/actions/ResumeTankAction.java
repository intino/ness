package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.box.slack.Helper;
import io.intino.ness.bus.BusService;
import io.intino.ness.datalake.TankStarter;
import io.intino.ness.graph.Tank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.intino.ness.box.actions.Action.OK;


public class ResumeTankAction {
	private static final Logger logger = LoggerFactory.getLogger(BusService.class);

	public NessBox box;
	public String tank;

	public ResumeTankAction() {
	}

	public ResumeTankAction(NessBox box, String tank) {
		this.box = box;
		this.tank = tank;
	}

	public String execute() {
		Tank aTank = Helper.findTank(box, tank);
		if (aTank == null) return "tank not found";
		new TankStarter(box.busManager(), box.datalakeManager()).start(aTank);
		box.busManager().pipe(aTank.feedQN(), aTank.flowQN());
//		box.restartBus(true);
		aTank.running(true);
		aTank.save$();
		return OK;
	}
}