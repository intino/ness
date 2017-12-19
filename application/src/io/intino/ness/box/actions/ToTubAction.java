package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.graph.Tank;


public class ToTubAction {

	public NessBox box;

	public void execute() {
		box.datalakeManager().seal(box.graph().tankList().toArray(new Tank[0]));
	}
}