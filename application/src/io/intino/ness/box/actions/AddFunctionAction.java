package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.graph.Function;

import java.util.List;

import static io.intino.ness.box.actions.Action.OK;
import static io.intino.ness.datalake.FunctionHelper.check;
import static io.intino.ness.datalake.FunctionHelper.compile;
import static java.util.stream.Collectors.toList;


public class AddFunctionAction {

	public NessBox box;
	public String name;
	public String code;

	public String execute() {
		List<Function> functions = box.graph().functionList(f -> f.name$().equals(name)).collect(toList());
		if (!functions.isEmpty()) return "Function name is already defined";
		String thePackage = packageOf(code);
		if (code.isEmpty() || !check(thePackage + "." + name, code))
			return "Code has errors or does not complies with MessageFunction interface";
		Function function = box.graph().create("functions", name).
				function(thePackage + "." + name, code, compile(thePackage + "." + name, code));
		function.save$();
		return OK;
	}

	private String packageOf(String sourceCode) {
		return sourceCode.substring(0, sourceCode.indexOf(";")).replaceAll("package |;", "");
	}
}