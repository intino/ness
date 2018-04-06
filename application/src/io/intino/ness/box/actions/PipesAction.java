package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.graph.Pipe;


public class PipesAction {

	public NessBox box;

	public String execute() {
		StringBuilder message = new StringBuilder();
		for (Pipe p : box.graph().pipeList())
			message.append(p.isTankSource() ? p.asTankSource().origin().qualifiedName() : p.asTopicSource().origin()).append(" -> ").append(p.destination().qualifiedName()).append("\n");
		return (message.length() == 0) ? "No pipes registered" : message.toString();
	}
}