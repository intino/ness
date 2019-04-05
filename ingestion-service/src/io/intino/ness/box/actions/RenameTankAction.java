package io.intino.ness.box.actions;

import io.intino.ness.box.NessServiceBox;
import io.intino.ness.box.Utils;
import io.intino.ness.graph.Datalake.Tank;

import static io.intino.ness.box.actions.Action.OK;

public class RenameTankAction {
	public NessServiceBox box;
	public String tank;
	public String name;

	public String execute() {
		Tank tankObject = Utils.findTank(box.graph(), tank);
		if (tankObject == null) return "Tank " + tank + " does not exist";
		if (Utils.findTank(box.datalake(), name) != null) return "Tank " + name + " already exists";
		tankObject.name(name);
		tankObject.save$();
		//box.datalake().rename(tankObject, name);TODO
		return Utils.findTank(box.datalake(), name) != null ? OK : "Impossible to rename tank";
	}
}