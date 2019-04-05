package io.intino.ness.triton.box.actions;

import io.intino.ness.triton.box.TritonBox;
import io.intino.ness.triton.graph.Pipe;


public class PipesAction {
	public TritonBox box;

	public String execute() {
		StringBuilder message = new StringBuilder();
		for (Pipe p : box.graph().pipeList())
			message.append(p.origin()).append(" -> ").append(p.destination()).append("\n");
		return (message.length() == 0) ? "No pipes registered" : message.toString();
	}
}