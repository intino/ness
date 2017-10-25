package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.graph.BusPipe;

import static io.intino.ness.box.actions.Action.OK;


public class StartBusPipeAction {

	public NessBox box;
	public String name;

	public String execute() {
		BusPipe aqueduct = box.ness().busPipeList(f -> f.name$().equals(name)).findFirst().orElse(null);
		if (aqueduct == null) return "Aqueduct not found";
		box.datalakeManager().startBusPipe(aqueduct);
		return OK;
	}
}