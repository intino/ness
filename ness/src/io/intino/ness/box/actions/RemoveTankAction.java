package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.datalake.Probes;
import io.intino.ness.graph.Tank;

import static io.intino.ness.box.actions.Action.OK;


public class RemoveTankAction {

	public NessBox box;
	public String name;

	public String execute() {
		Tank tank = box.graph().tankList().stream().filter(t -> t.name().equalsIgnoreCase(name)).findFirst().orElse(null);
		if (tank == null) return "Tank not found";
		//TODO remove tank from datalake
		box.busManager().stopConsumersOf(Probes.feed(tank));
		box.busManager().stopConsumersOf(Probes.put(tank));
		return OK;
	}

}