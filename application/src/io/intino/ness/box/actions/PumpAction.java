package io.intino.ness.box.actions;

import io.intino.ness.Function;
import io.intino.ness.box.NessBox;

import java.util.List;

import static java.util.stream.Collectors.toList;


public class PumpAction extends Action {

	public NessBox box;
	public String functionName;
	public String input;
	public String output;

	public String execute() {
		List<Function> functions = box.ness().functionList(f -> f.name$().equals(functionName)).collect(toList());
		if (functions.isEmpty()) return "Function not found";
		Function function = functions.get(0);
		box.datalakeManager().pump(function, input, output);
		return OK;
	}


}