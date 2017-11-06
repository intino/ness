package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.bus.BusPipeManager;
import io.intino.ness.graph.BusPipe;

import java.util.List;
import java.util.stream.Collectors;


public class BusPipesAction {

	public NessBox box;

	public List<String> execute() {
		return box.ness().busPipeList().stream().map(bp -> bp.name$() + ": " + (isRunning(bp) ? "Running" : "Stopped")).collect(Collectors.toList());
	}

	private boolean isRunning(BusPipe busPipe) {
		for (BusPipeManager manager : box.busPipeManagers())
			if (manager.busPipes().contains(busPipe))
				return manager.isRunning();
		return false;
	}
}