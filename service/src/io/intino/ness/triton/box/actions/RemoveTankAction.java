package io.intino.ness.triton.box.actions;

import io.intino.ness.triton.box.ServiceBox;

import static io.intino.ness.triton.box.actions.Action.OK;

public class RemoveTankAction {

	public ServiceBox box;
	public String name;

	public String execute() {
		io.intino.ness.triton.graph.Tank tank = box.graph().tankList().stream().filter(t -> t.name().equalsIgnoreCase(name)).findFirst().orElse(null);
		if (tank == null) return "Tank not found";
		//TODO remove tank from datalake
		box.busManager().stopConsumersOf(tank.name());
		return OK;
	}

}