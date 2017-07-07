package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.graph.Tank;

import java.util.List;

import static io.intino.ness.box.actions.Action.OK;
import static java.util.stream.Collectors.toList;


public class AddTankAction {

	public NessBox box;
	public String name;

	public String execute() {
		List<Tank> tanks = box.ness().tankList(t -> t.name$().equals(this.name)).collect(toList());
		if (!tanks.isEmpty()) return "Tank already exist";
		String name = this.name.replaceFirst("feed\\.", "");
		Tank newTank = box.ness().create("tanks").tank(name);
		box.datalakeManager().registerTank(newTank);
		box.datalakeManager().feedFlow(newTank);
		newTank.save$();
		return OK;
	}


}