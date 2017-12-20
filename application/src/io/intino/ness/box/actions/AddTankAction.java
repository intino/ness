package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.datalake.DatalakeManager;
import io.intino.ness.graph.NessGraph;
import io.intino.ness.graph.Tank;

import java.util.List;

import static io.intino.ness.box.actions.Action.OK;
import static java.util.stream.Collectors.toList;


public class AddTankAction {

	public NessBox box;
	public String name;

	public String execute() {
		if (name.isEmpty()) return "Tank is empty";
		String tankName = name.startsWith("feed.") ? name.replaceFirst("feed\\.", "") : name;
		NessGraph ness = ness();
		List<Tank> tanks = ness.tankList(t -> t.qualifiedName().equals(tankName)).collect(toList());
		if (!tanks.isEmpty()) return "Tank already exist";
		registerTank(ness);
		return OK;
	}

	private void registerTank(NessGraph ness) {
		Tank tank = ness.create("tanks").tank(name.replace(".", "-")).qualifiedName(name);
		datalake().addTank(tank);
		box.busManager().getOrCreateTopic(tank.feedQN());
		box.busManager().getOrCreateTopic(tank.flowQN());
		box.busManager().getOrCreateTopic(tank.dropQN());
		resumeTank(name);
		tank.save$();
	}

	private void resumeTank(String tankName) {
		final ResumeTankAction action = new ResumeTankAction();
		action.box = box;
		action.tank = tankName;
		action.execute();
	}

	private NessGraph ness() {
		return box.graph();
	}

	private DatalakeManager datalake() {
		return box.datalakeManager();
	}
}