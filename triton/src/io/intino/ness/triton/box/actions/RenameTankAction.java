package io.intino.ness.triton.box.actions;

import io.intino.ness.triton.box.TritonBox;
import io.intino.ness.triton.box.Utils;

import static io.intino.ness.triton.box.actions.Action.OK;

public class RenameTankAction {
	public TritonBox box;
	public String tank;
	public String name;

	public String execute() {
		io.intino.ness.triton.graph.Tank tankObject = Utils.findTank(box.graph(), tank);
		if (tankObject == null) return "Tank " + tank + " does not exist";
		if (Utils.findTank(box.datalake(), name) != null) return "Tank " + name + " already exists";
		tankObject.name(name);
		tankObject.save$();
		//box.datalake().rename(tankObject, name);TODO
		return Utils.findTank(box.datalake(), name) != null ? OK : "Impossible to rename tank";
	}
}