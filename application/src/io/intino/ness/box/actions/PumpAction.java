package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.graph.Function;


public class PumpAction extends Action {

	public NessBox box;
	public String functionName;
	public String input;
	public String output;

	public String execute() {
		Function function = box.graph().functionList(f -> f.name$().equals(functionName)).findFirst().orElse(null);
		if (function == null) return "Function not found";
		if (box.graph().tank(input) == null || box.graph().tank(output) == null) return "Function not found";
		box.datalakeManager().pump(box.graph().tank(input), box.graph().tank(output), function);
		return OK;
	}
}