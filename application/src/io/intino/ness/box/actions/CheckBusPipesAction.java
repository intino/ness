package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.bus.BusPipeManager;
import io.intino.ness.graph.BusPipe;


public class CheckBusPipesAction {

	public NessBox box;


	public void execute() {
		for (BusPipe busPipe : box.ness().busPipeList()) {
			if (!isRunning(busPipe)) busPipeManagerOf(busPipe).start(busPipe);
		}
	}

	private BusPipeManager busPipeManagerOf(BusPipe busPipe) {
		for (BusPipeManager manager : box.busPipeManagers())
			if (manager.busPipes().contains(busPipe))
				return manager;
		return null;
	}

	private boolean isRunning(BusPipe busPipe) {
		for (BusPipeManager manager : box.busPipeManagers())
			if (manager.busPipes().contains(busPipe))
				return manager.isRunning();
		return false;
	}
}