package io.intino.ness.triton.box.actions;

import io.intino.ness.triton.box.TritonBox;
import io.intino.ness.triton.datalake.Probes;
import io.intino.ness.triton.graph.Datalake.Tank;

import static io.intino.ness.triton.box.actions.Action.OK;


public class RemoveTankAction {

	public TritonBox box;
	public String name;

	public String execute() {
		Tank tank = box.graph().datalake().tankList().stream().filter(t -> t.name().equalsIgnoreCase(name)).findFirst().orElse(null);
		if (tank == null) return "Tank not found";
		//TODO remove tank from datalake
		box.busManager().stopConsumersOf(Probes.feed(tank));
		box.busManager().stopConsumersOf(Probes.put(tank));
		return OK;
	}

}