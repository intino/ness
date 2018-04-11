package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;

import static io.intino.ness.box.actions.Action.OK;


public class RemovePipesAction {
	public NessBox box;
	public String origin;

	public String execute() {
		box.nessGraph().clear().pipe(p -> p.origin().equals(origin));
		return OK;
	}
}