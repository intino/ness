package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.box.slack.Helper;
import io.intino.ness.datalake.graph.Tank;

import static io.intino.ness.box.actions.Action.OK;


public class RenameTankAction {
	public NessBox box;
	public String tank;
	public String name;

	public String execute() {
		Tank tankObject = Helper.findTank(box.datalake(), tank);
		if (tankObject == null) return "Tank " + tank + " does not exist";
		if (Helper.findTank(box.datalake(), name) != null) return "Tank " + name + " already exists";
		tankObject.qualifiedName(name);
		tankObject.save$();
		box.datalake().rename(tankObject, name);
		return box.datalake().tank(name) != null ? OK : "Impossible to rename tank";
	}
}