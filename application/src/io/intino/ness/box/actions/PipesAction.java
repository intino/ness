package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.graph.Pipe;


public class PipesAction {

	public NessBox box;

	public String execute() {
		String message = "";
		for (Pipe p : box.ness().pipeList())
			message += p.origin() + " -> " + p.destination() + "\n";
		return message.isEmpty() ? "No pipes registered" : message;
	}
}