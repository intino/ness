package io.intino.ness.triton.box.actions;

import io.intino.ness.triton.box.ServiceBox;
import io.intino.ness.triton.graph.ExternalBus;


public class AddExternalBusAction {

	public ServiceBox box;
	public String name;
	public String externalBusUrl;
	public String user;
	public String password;

	public AddExternalBusAction() {
	}

	public AddExternalBusAction(ServiceBox box, String name, String externalBusUrl, String user, String password) {
		this.box = box;
		this.name = name;
		this.externalBusUrl = externalBusUrl;
		this.user = user;
		this.password = password;
	}

	public String execute() {
		ExternalBus bus = box.graph().externalBusList(f -> f.name$().equals(name)).findFirst().orElse(null);
		if (bus != null) return "External Bus is already defined";
		ExternalBus externalBus = box.graph().create("external-buses", name).externalBus(externalBusUrl.replaceAll("<|>", ""), box.busManager().nessID(), user, password);
		externalBus.save$();
		return Action.OK;
	}
}