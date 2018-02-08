package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;
import io.intino.ness.datalake.PipeStarter;
import io.intino.ness.graph.Function;
import io.intino.ness.graph.NessGraph;
import io.intino.ness.graph.Pipe;
import io.intino.ness.graph.Tank;


public class AddPipeAction extends Action {

	public NessBox box;
	public String from;
	public String to;
	public String functionName;

	public String execute() {
		Function function = ness().functionList(t -> t.name$().equals(functionName)).findFirst().orElse(null);
		if (function == null) return "Function not found";
		Tank tankFrom = ness().tank(from);
		if (tankFrom == null) return "Source tank not found";
		Tank tankTo = ness().tank(from);
		if (tankTo == null) return "Destination tank not found";
		final Pipe pipe = ness().create("Pipes").pipe(tankFrom, tankTo);
		pipe.transformer(function);
		new PipeStarter(box.busManager()).start(pipe);
		pipe.save$();
		return OK;
	}

	private NessGraph ness() {
		return box.graph();
	}
}