package io.intino.ness.triton.box.actions;

import io.intino.ness.triton.box.ServiceBox;
import io.intino.ness.triton.graph.Tank;
import io.intino.ness.triton.graph.TritonGraph;

import java.util.List;
import java.util.stream.Collectors;


public class AddTankAction {

	public ServiceBox box;
	public String name;

	public AddTankAction() {
	}

	public AddTankAction(ServiceBox box, String name) {
		this.box = box;
		this.name = name;
	}

	public String execute() {
		if (name.isEmpty()) return "Tank is empty";
		String tankName = name.startsWith("feed.") ? name.replaceFirst("feed\\.", "") : name;
		TritonGraph tritonGraph = box.graph();
		List<Tank> tanks = tritonGraph.tankList(t -> t.name().equals(tankName)).collect(Collectors.toList());
		if (!tanks.isEmpty()) return "Tank already exist";
		registerTank();
		return Action.OK;
	}

	private void registerTank() {
		final Tank tank = box.graph().create("tanks").tank(name, Tank.Type.Event);
		box.datalake().eventStore().tank(name);
		box.busManager().getOrCreateTopic(tank.name());
		resumeTank(name);
	}

	private void resumeTank(String tankName) {
		new ResumeTankAction(box, tankName).execute();
	}
}