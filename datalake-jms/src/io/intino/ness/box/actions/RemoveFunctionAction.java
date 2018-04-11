package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.graph.Function;
import io.intino.ness.graph.NessGraph;

import java.util.List;

import static io.intino.ness.box.actions.Action.OK;
import static java.util.stream.Collectors.toList;


public class RemoveFunctionAction {

	public NessBox box;
	public String name;

	public String execute() {
		List<Function> functions = ness().functionList(t -> t.name$().equals(name)).collect(toList());
		if (functions.isEmpty()) return "Function not found";
		for (Function function : functions) function.delete$();
		return OK;
	}

	private NessGraph ness() {
		return box.nessGraph();
	}
}