package io.intino.ness.box.actions;

import io.intino.konos.jms.TopicConsumer;
import io.intino.ness.box.NessBox;
import io.intino.ness.box.slack.Helper;
import io.intino.ness.datalake.graph.Tank;

import java.util.List;

import static io.intino.ness.box.actions.Action.OK;


public class PauseTankAction {
	public NessBox box;
	public String tank;

	public PauseTankAction() {
	}

	public PauseTankAction(NessBox box, String tank) {
		this.box = box;
		this.tank = tank;
	}

	public String execute() {
		Tank datalakeTank = Helper.findTank(box.datalake(), tank);
		io.intino.ness.graph.Tank jmsTank = Helper.findTank(box.nessGraph(), tank);
		if (datalakeTank == null) return "tank not found";
		return execute(datalakeTank, jmsTank);
	}

	public String execute(Tank aTank, io.intino.ness.graph.Tank jmsTank) {
		List<TopicConsumer> consumers = box.busManager().consumersOf(aTank.feedQN());
		consumers.forEach(TopicConsumer::stop);
		aTank.active(false);
		jmsTank.active(false);
		aTank.save$();
		jmsTank.save$();
		return OK;
	}
}