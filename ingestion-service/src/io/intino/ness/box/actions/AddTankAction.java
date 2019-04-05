package io.intino.ness.box.actions;

import io.intino.ness.box.NessServiceBox;
import io.intino.ness.datalake.Probes;
import io.intino.ness.graph.Datalake.Tank;
import io.intino.ness.graph.NessGraph;

import java.util.List;

import static io.intino.ness.box.actions.Action.OK;


public class AddTankAction {

	public NessServiceBox box;
	public String name;

	public AddTankAction() {
	}

	public AddTankAction(NessServiceBox box, String name) {
		this.box = box;
		this.name = name;
	}

	public String execute() {
		if (name.isEmpty()) return "Tank is empty";
		String tankName = name.startsWith("feed.") ? name.replaceFirst("feed\\.", "") : name;
		NessGraph nessGraph = box.graph();
		List<Tank> tanks = nessGraph.datalake().tankList(t -> t.name().equals(tankName));
		if (!tanks.isEmpty()) return "Tank already exist";
		registerTank();
		return OK;
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