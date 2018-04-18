package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.datalake.PipeStarter;
import io.intino.ness.datalake.graph.DatalakeGraph;
import io.intino.ness.graph.Function;
import io.intino.ness.graph.NessGraph;
import io.intino.ness.graph.Pipe;


public class AddPipeAction extends Action {

	public NessBox box;
	public String from;
	public String to;
	public String functionName;

	public String execute() {
		Function function = ness().functionList(t -> t.name$().equals(functionName)).findFirst().orElse(null);
		final Pipe pipe = ness().create("pipes").pipe(from, to);
		if (!functionName.isEmpty()) {
			if (function != null) pipe.transformer(function);
			else return "Function not found";
		}
		new PipeStarter(box.busManager()).start(pipe);
		pipe.save$();
		return OK;
	}

	private NessGraph ness() {
		return box.nessGraph();
	}
}