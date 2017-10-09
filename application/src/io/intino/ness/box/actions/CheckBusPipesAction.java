package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.datalake.DatalakeManager;
import io.intino.ness.graph.Aqueduct;


public class CheckBusPipesAction {

	public NessBox box;


	public void execute() {
		DatalakeManager datalakeManager = box.datalakeManager();
		for (Aqueduct aqueduct : box.ness().aqueductList()) {
			if (!datalakeManager.status(aqueduct)) datalakeManager.startBusPipe(aqueduct);
		}
	}
}