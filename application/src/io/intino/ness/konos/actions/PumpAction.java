package io.intino.ness.konos.actions;

import io.intino.ness.Function;
import io.intino.ness.Ness;
import io.intino.ness.konos.NessBox;

import java.util.List;

import static io.intino.ness.konos.slack.Helper.ness;


public class PumpAction extends Action {

	public NessBox box;
	public String functionName;
	public String input;
	public String output;

	public String execute() {
		Ness ness = ness(box);
		List<Function> functions = ness.functionList(f -> f.name().equals(functionName));
		if (functions.isEmpty()) return "Function not found";
		Function function = functions.get(0);
		datalake(box).pump(function, input, output);
		return OK;
	}


}