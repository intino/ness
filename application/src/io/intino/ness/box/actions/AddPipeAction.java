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
		if (!functionName.isEmpty()) if (function == null) return "Function not found";
		Tank tankFrom = ness().tank(from);
		Tank tankTo = ness().tank(to);
		if (tankTo == null) return "Destination tank not found";
		final Pipe pipe = ness().create("pipes").pipe(tankTo);
		if (tankFrom != null) pipe.asTankSource(tankFrom);
		else pipe.asTopicSource(from);
		if (function != null) pipe.transformer(function);
		new PipeStarter(box.busManager()).start(pipe);
		pipe.save$();
		return OK;
	}

	private NessGraph ness() {
		return box.graph();
	}
}