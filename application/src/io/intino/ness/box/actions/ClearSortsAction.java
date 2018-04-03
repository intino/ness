package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.graph.Tank;

public class ClearSortsAction {

	public NessBox box;

	public void execute() {
		for (Tank tank : box.graph().tankList()) {
			tank.sorted().clear();
			tank.save$();
		}
	}
}