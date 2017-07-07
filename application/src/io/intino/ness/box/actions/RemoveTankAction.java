package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.graph.Tank;

import static io.intino.ness.box.actions.Action.OK;


public class RemoveTankAction {

	public NessBox box;
	public String name;

	public String execute() {
		Tank tank = box.ness().tankList(t -> t.name$().equals(name)).findFirst().orElse(null);
		if (tank != null) {
			tank.delete$();
			box.datalakeManager().removeTank(tank);
			return OK;
		} else return "Tank not found";
	}
}