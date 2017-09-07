package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;

import java.util.List;

import static io.intino.ness.box.actions.Action.OK;


public class PipeAction {

	public NessBox box;
	public String from;
	public String to;

	public String execute() {
		List<String> topics = box.busManager().topics();
		if (!topics.contains(from)) return "Origin topic not found";
		if (box.busManager().pipe(from, to)) {
			box.ness().create("pipes").pipe(from, to).save$();
		}
		return OK;
	}
}