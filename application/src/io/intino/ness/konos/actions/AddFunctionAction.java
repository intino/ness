package io.intino.ness.konos.actions;

import io.intino.ness.Function;
import io.intino.ness.Ness;
import io.intino.ness.konos.NessBox;

import java.util.List;

import static io.intino.ness.konos.slack.Helper.downloadFile;
import static io.intino.ness.konos.slack.Helper.ness;


public class AddFunctionAction extends Action {

	public NessBox box;
	public String name;
	public String code;

	public String execute() {
		Ness ness = ness(box);
		String sourceCode = downloadFile(code);
		List<Function> functions = ness.functionList(f -> f.name().equals(name));
		if (!functions.isEmpty()) return "function name is already defined";
		if (!datalake(box).isCorrect(code)) return "Code has errors or does not complies with NessFunction interface";
		Function function = ness.create("functions", name).function(sourceCode);
		function.save();
		return OK;	}


}