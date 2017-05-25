package io.intino.ness.box.actions;

import io.intino.ness.Tank;
import io.intino.ness.box.NessBox;
import io.intino.ness.box.slack.Helper;


public class RenameAction extends Action {

	public NessBox box;
	public String tank;
	public String name;

	public String execute() {
		Tank tank = Helper.findTank(box, this.tank);
		if (tank == null) return "Please select a tank";
		return box.busManager().renameTopic(tank.qualifiedName(), name) ? OK : "Impossible to rename tank";
	}
}