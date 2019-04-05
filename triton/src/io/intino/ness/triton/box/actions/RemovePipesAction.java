package io.intino.ness.triton.box.actions;

import io.intino.ness.triton.box.TritonBox;

import static io.intino.ness.triton.box.actions.Action.OK;


public class RemovePipesAction {
	public TritonBox box;
	public String origin;

	public String execute() {
		box.graph().clear().pipe(p -> p.origin().equals(origin));
		return OK;
	}
}