package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.graph.Function;


public class PumpAction extends Action {

	public NessBox box;
	public String functionName;
	public String input;
	public String output;

	public String execute() {
		Function function = box.ness().functionList(f -> f.name$().equals(functionName)).findFirst().orElse(null);
		box.datalakeManager().pump(input, output, function);
		return OK;
	}
}