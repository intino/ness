package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.datalake.graph.DatalakeGraph;
import io.intino.ness.graph.NessGraph;
import io.intino.ness.datalake.graph.Tank;

import java.util.List;

import static io.intino.ness.box.actions.Action.OK;
import static java.util.stream.Collectors.toList;


public class RemoveTankAction {

	public NessBox box;
	public String name;

	public String execute() {
		List<Tank> tanks = datalake().tankList(t -> t.qualifiedName().equals(name)).collect(toList());
		if (tanks.isEmpty()) return "Tank not found";
		for (Tank tank : tanks) {
			box.busManager().stopConsumersOf(tank.feedQN());
			box.busManager().stopConsumersOf(tank.dropQN());
			datalake().remove(tank);
		}
		return OK;
	}

	private DatalakeGraph datalake() {
		return box.datalake();
	}

}