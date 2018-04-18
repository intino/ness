package io.intino.ness.box.actions;

import io.intino.konos.jms.TopicConsumer;
import io.intino.ness.box.NessBox;
import io.intino.ness.box.slack.Helper;
import io.intino.ness.datalake.graph.Tank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		Tank aTank = Helper.findTank(box.datalake(), tank);
		if (aTank == null) return "tank not found";
		return execute(aTank);
	}

	public String execute(Tank aTank) {
		List<TopicConsumer> consumers = box.busManager().consumersOf(aTank.feedQN());
		consumers.forEach(TopicConsumer::stop);
		aTank.active(false);
		return OK;
	}
}