package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.datalake.DatalakeManager;
import io.intino.ness.graph.BusPipe;


public class CheckBusPipesAction {

	public NessBox box;


	public void execute() {
		DatalakeManager datalakeManager = box.datalakeManager();
		for (BusPipe aqueduct : box.ness().busPipeList()) {
			if (!datalakeManager.status(aqueduct)) datalakeManager.startBusPipe(aqueduct);
		}
	}
}