package io.intino.ness.triton.box.actions;

import io.intino.ness.triton.box.TritonBox;
import io.intino.ness.triton.datalake.Probes;
import io.intino.ness.triton.graph.Datalake.Tank;
import io.intino.ness.triton.graph.TritonGraph;

import java.util.List;


public class AddTankAction {

	public TritonBox box;
	public String name;

	public AddTankAction() {
	}

	public AddTankAction(TritonBox box, String name) {
		this.box = box;
		this.name = name;
	}

	public String execute() {
		if (name.isEmpty()) return "Tank is empty";
		String tankName = name.startsWith("feed.") ? name.replaceFirst("feed\\.", "") : name;
		TritonGraph tritonGraph = box.graph();
		List<Tank> tanks = tritonGraph.datalake().tankList(t -> t.name().equals(tankName));
		if (!tanks.isEmpty()) return "Tank already exist";
		registerTank();
		return Action.OK;
	}

	private void registerTank() {
		final Tank tank = box.graph().datalake().create("tanks").tank(name);
		box.datalake().eventStore().tank(name);
		box.busManager().getOrCreateTopic(Probes.feed(tank));
		box.busManager().getOrCreateTopic(Probes.flow(tank));
		box.busManager().getOrCreateTopic(Probes.put(tank));
		resumeTank(name);
	}

	private void resumeTank(String tankName) {
		new ResumeTankAction(box, tankName).execute();
	}
}