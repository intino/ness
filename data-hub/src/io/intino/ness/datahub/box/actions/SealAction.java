package io.intino.ness.datahub.box.actions;

import io.intino.ness.datahub.box.DataHubBox;


public class SealAction {

	public DataHubBox box;
	public io.intino.alexandria.core.Context context = new io.intino.alexandria.core.Context();

	public void execute() {
		new Thread(() -> box.datahub().sessionSealer().seal()).start();
	}
}