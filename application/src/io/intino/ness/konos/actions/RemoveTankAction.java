package io.intino.ness.konos.actions;

import io.intino.ness.Ness;
import io.intino.ness.Tank;
import io.intino.ness.konos.NessBox;

import java.util.List;


public class RemoveTankAction extends Action{

	public NessBox box;
	public String name;

	public String execute() {
		Ness wrapper = box.graph().wrapper(Ness.class);
		List<Tank> tanks = wrapper.tankList(t -> t.qualifiedName().equals(this.name));
		if (tanks.isEmpty()) return "Tank not found";
		for (Tank tank : tanks) {
			datalake(box).removeTank(tank);
			tank.delete();
		}
		return OK;
	}
}