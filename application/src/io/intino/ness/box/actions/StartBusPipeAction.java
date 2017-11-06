package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.bus.BusPipeManager;
import io.intino.ness.graph.BusPipe;

import static io.intino.ness.box.actions.Action.OK;


public class StartBusPipeAction {

	public NessBox box;
	public String name;

	public String execute() {
		BusPipe busPipe = box.ness().busPipeList(f -> f.name$().equals(name)).findFirst().orElse(null);
		if (busPipe == null) return "Bus pipe not found";
		for (BusPipeManager manager : box.busPipeManagers())
			if (manager.busPipes().contains(busPipe) && !manager.isRunning()) manager.start();
		return OK;
	}
}