package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.datalake.FunctionManager;
import io.intino.ness.graph.Function;

import java.util.List;

import static io.intino.ness.box.actions.Action.OK;
import static java.util.stream.Collectors.toList;


public class AddFunctionAction {

	public NessBox box;
	public String name;
	public String code;

	public String execute() {
		List<Function> functions = box.ness().functionList(f -> f.name$().equals(name)).collect(toList());
		if (!functions.isEmpty()) return "Function name is already defined";
		if (code.isEmpty() || !FunctionManager.check(name, code))
			return "Code has errors or does not complies with MessageFunction interface";
		Function function = box.ness().create("functions", name).function(packageOf(code) + "." + name, code);
		function.save$();
		return OK;
	}


	private String packageOf(String sourceCode) {
		return sourceCode.substring(0, sourceCode.indexOf("\n")).replaceAll("package |;", "");
	}

}