package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.datalake.DatalakeManager;
import io.intino.ness.graph.NessGraph;
import io.intino.ness.graph.Tank;

import java.util.List;

import static io.intino.ness.box.actions.Action.OK;
import static java.util.stream.Collectors.toList;


public class RemoveTankAction {

	public NessBox box;
	public String name;

	public String execute() {
		NessGraph wrapper = ness();
		List<Tank> tanks = wrapper.tankList(t -> t.qualifiedName().equals(name)).collect(toList());
		if (tanks.isEmpty()) return "Tank not found";
		for (Tank tank : tanks) {
			datalake().removeTank(tank);
			tank.delete$();
		}
		return OK;
	}

	private NessGraph ness() {
		return box.graph();
	}

	private DatalakeManager datalake() {
		return box.datalakeManager();
	}

}