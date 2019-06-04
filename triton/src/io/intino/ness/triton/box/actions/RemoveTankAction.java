package io.intino.ness.triton.box.actions;

import io.intino.ness.triton.box.TritonBox;
import io.intino.ness.triton.datalake.Probes;

import static io.intino.ness.triton.box.actions.Action.OK;

public class RemoveTankAction {

	public TritonBox box;
	public String name;

	public String execute() {
		io.intino.ness.triton.graph.Tank tank = box.graph().tankList().stream().filter(t -> t.name().equalsIgnoreCase(name)).findFirst().orElse(null);
		if (tank == null) return "Tank not found";
		//TODO remove tank from datalake
		box.busManager().stopConsumersOf(Probes.feed(tank));
		box.busManager().stopConsumersOf(Probes.put(tank));
		return OK;
	}

}