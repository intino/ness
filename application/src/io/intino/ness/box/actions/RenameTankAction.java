package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.box.slack.Helper;
import io.intino.ness.graph.Tank;

import static io.intino.ness.box.actions.Action.OK;


public class RenameTankAction {

	public NessBox box;
	public String tank;
	public String name;

	public String execute() {
		Tank tankObject = Helper.findTank(box, tank);
		if (tankObject == null) return "Please select a tank";
		return box.datalakeManager().rename(tankObject, name) ? OK : "Impossible to rename tank";
	}
}