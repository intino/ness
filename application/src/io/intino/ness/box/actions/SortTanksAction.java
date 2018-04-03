package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;


public class SortTanksAction {

	public NessBox box;

	public void execute() {
		box.graph().tankList().forEach(tank -> box.datalakeManager().sort(tank));
	}
}