package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.bus.BusPipeManager;
import io.intino.ness.graph.BusPipe;

import static io.intino.ness.box.actions.Action.OK;


public class StopBusPipeAction {

	public NessBox box;
	public String name;

	public String execute() {
		BusPipe busPipe = box.ness().busPipeList(f -> f.name$().equals(name)).findFirst().orElse(null);
		if (busPipe == null) return "Bus pipe not found";
		busPipeManagerOf(busPipe).stop();
		return OK;
	}


	private BusPipeManager busPipeManagerOf(BusPipe busPipe) {
		for (BusPipeManager manager : box.busPipeManagers())
			if (manager.busPipes().contains(busPipe))
				return manager;
		return null;
	}
}