package io.intino.ness.konos.actions;

import io.intino.ness.Ness;
import io.intino.ness.Tank;
import io.intino.ness.konos.NessBox;

import java.util.List;


public class AddTankAction extends Action {

	public NessBox box;
	public String name;

	public String execute() {
		Ness ness = box.graph().wrapper(Ness.class);
		List<Tank> tanks = ness.tankList(t -> t.name().equals(this.name));
		if (!tanks.isEmpty()) return "Tank already exist";
		String name = this.name.replaceFirst("feed\\.", "");
		Tank newTank = ness.create("tanks").tank(name);
		datalake(box).registerTank(newTank);
		datalake(box).feedFlow(newTank);
		newTank.save();
		return OK;
	}
}