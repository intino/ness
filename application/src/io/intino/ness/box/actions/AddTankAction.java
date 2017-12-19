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
		Tank newTank = ness.create("tanks").tank(tankName.replace(".", "-"));
		newTank.qualifiedName(tankName);
		datalake().registerTank(newTank);
		newTank.save$();
		return OK;
	}

	private NessGraph ness() {
		return box.graph();
	}

	private DatalakeManager datalake() {
		return box.datalakeManager();
	}
}