package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.graph.Function;
import io.intino.ness.graph.Pipe;

import java.util.List;

import static io.intino.ness.box.actions.Action.OK;


public class AddPipeAction {

	public NessBox box;
	public String from;
	public String to;
	public String functionName;

	public String execute() {
		Function function = box.ness().functionList(f -> f.name$().equals(functionName)).findFirst().orElse(null);
		List<String> topics = box.busManager().topics();
		if (!topics.contains(from)) return "Origin topic not found";
		if (box.datalakeManager().pipe(from, to, function)) {
			Pipe newPipe = box.ness().create("pipes").pipe(from, to);
			if (function != null) newPipe.transformer(function);
			newPipe.save$();
		}
		return OK;
	}
}