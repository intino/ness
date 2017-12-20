package io.intino.ness.box.actions;

import io.intino.konos.jms.TopicConsumer;
import io.intino.ness.box.NessBox;
import io.intino.ness.box.slack.Helper;
import io.intino.ness.graph.Tank;

import java.util.List;

import static io.intino.ness.box.actions.Action.OK;


public class PauseTankAction {

	public NessBox box;
	public String tank;

	public String execute() {
		Tank aTank = Helper.findTank(box, tank);
		if (aTank == null) return "tank not found";
		List<TopicConsumer> consumers = box.busManager().consumersOf(aTank.feedQN());
		if (!consumers.isEmpty()) consumers.forEach(TopicConsumer::stop);
		box.busManager().stopPipe(aTank.feedQN(), aTank.flowQN());
		return OK;
	}
}