package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.graph.Aqueduct;

import static io.intino.ness.box.actions.Action.OK;


public class StopAqueductAction {

	public NessBox box;
	public String name;

	public String execute() {
		Aqueduct aqueduct = box.ness().aqueductList(f -> f.name$().equals(name)).findFirst().orElse(null);
		if (aqueduct == null) return "Aqueduct not found";
		box.datalakeManager().stopAqueduct(aqueduct);
		return OK;
	}
}