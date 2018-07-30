package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.datalake.graph.DatalakeGraph;
import io.intino.ness.datalake.graph.Tank;

import java.util.List;

import static io.intino.ness.box.actions.Action.OK;
import static java.util.stream.Collectors.toList;


public class AddTankAction {

	public NessBox box;
	public String name;

	public AddTankAction() {
	}

	public AddTankAction(NessBox box, String name) {
		this.box = box;
		this.name = name;
	}

	public String execute() {
		if (name.isEmpty()) return "Tank is empty";
		String tankName = name.startsWith("feed.") ? name.replaceFirst("feed\\.", "") : name;
		DatalakeGraph datalake = datalake();
		List<Tank> tanks = datalake.tankList(t -> t.qualifiedName().equals(tankName)).collect(toList());
		if (!tanks.isEmpty()) return "Tank already exist";
		registerTank();
		return OK;
	}

	private void registerTank() {
		final Tank tank = datalake().add(name);
		box.busManager().getOrCreateTopic(tank.feedQN());
		box.busManager().getOrCreateTopic(tank.flowQN());
		box.busManager().getOrCreateTopic(tank.putQN());
		resumeTank(name);
	}

	private void resumeTank(String tankName) {
		new ResumeTankAction(box, tankName).execute();
	}

	private DatalakeGraph datalake() {
		return box.datalake();
	}
}