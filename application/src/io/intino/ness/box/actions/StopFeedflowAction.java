package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.box.slack.Helper;
import io.intino.ness.graph.Tank;

import static io.intino.ness.box.actions.Action.OK;


public class StopFeedflowAction {

	public NessBox box;
	public String tank;

	public String execute() {
		Tank aTank = Helper.findTank(box, tank);
		if (aTank == null) return "tank not found";
		box.datalakeManager().stopFeed(aTank);
		return OK;
	}
}