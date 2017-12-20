package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.box.slack.Helper;
import io.intino.ness.graph.Tank;
import io.intino.ness.inl.Message;

import static io.intino.konos.jms.Consumer.textFrom;
import static io.intino.ness.box.actions.Action.OK;
import static io.intino.ness.inl.Message.load;


public class ResumeTankAction {

	public NessBox box;
	public String tank;

	public String execute() {
		Tank aTank = Helper.findTank(box, tank);
		if (aTank == null) return "tank not found";
		box.busManager().registerConsumer(aTank.feedQN(), message -> drop(aTank, load(textFrom(message))));
		box.busManager().pipe(aTank.feedQN(), aTank.flowQN());
		return OK;
	}

	private void drop(Tank aTank, Message message) {
		box.datalakeManager().station().drop(aTank.qualifiedName()).register(message);
	}
}