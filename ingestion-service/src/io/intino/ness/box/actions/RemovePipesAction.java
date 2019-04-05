package io.intino.ness.box.actions;

import io.intino.ness.box.NessServiceBox;

import static io.intino.ness.box.actions.Action.OK;


public class RemovePipesAction {
	public NessServiceBox box;
	public String origin;

	public String execute() {
		box.graph().clear().pipe(p -> p.origin().equals(origin));
		return OK;
	}
}