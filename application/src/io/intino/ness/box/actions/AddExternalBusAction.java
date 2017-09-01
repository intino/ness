package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.graph.ExternalBus;

import static io.intino.ness.box.actions.Action.OK;


public class AddExternalBusAction {

	public NessBox box;
	public String name;
	public String externalBusUrl;
	public String user;
	public String password;

	public String execute() {
		ExternalBus bus = box.ness().externalBusList(f -> f.name$().equals(name)).findFirst().orElse(null);
		if (bus != null) return "External Bus is already defined";
		box.ness().create("external-buses", name).externalBus(externalBusUrl.replaceAll("<|>", ""), user, password).save$();
		return OK;
	}
}