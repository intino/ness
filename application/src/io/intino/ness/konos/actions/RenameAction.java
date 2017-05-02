package io.intino.ness.konos.actions;

import io.intino.ness.Tank;
import io.intino.ness.bus.BusManager;
import io.intino.ness.konos.NessBox;

import static io.intino.ness.konos.slack.Helper.findTank;


public class RenameAction extends Action {

	public NessBox box;
	public String tank;
	public String name;

	public String execute() {
		Tank tank = findTank(box, this.tank);
		if (tank == null) return "Please select a tank";
		return box.get(BusManager.class).renameTopic(tank.qualifiedName(), name) ? OK : "Impossible to rename tank";
	}


}