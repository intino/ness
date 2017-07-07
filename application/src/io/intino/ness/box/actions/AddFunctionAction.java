package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.graph.Function;
import io.intino.ness.graph.NessGraph;

import java.util.List;

import static io.intino.ness.box.actions.Action.OK;
import static io.intino.ness.box.slack.Helper.downloadFile;
import static java.util.stream.Collectors.toList;


public class AddFunctionAction {

	public NessBox box;
	public String name;
	public String code;

	public String execute() {
		NessGraph ness = box.ness();
		String sourceCode = downloadFile(code);
		List<Function> functions = ness.functionList(f -> f.name$().equals(name)).collect(toList());
		if (!functions.isEmpty()) return "function name is already defined";
		if (!box.datalakeManager().isCorrect(code))
			return "Code has errors or does not complies with NessFunction interface";
		Function function = ness.create("functions", name).function(sourceCode);
		function.save$();
		return OK;
	}
}