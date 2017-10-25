package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;

import java.util.List;
import java.util.stream.Collectors;


public class BusPipesAction {

	public NessBox box;

	public List<String> execute() {
		return box.ness().busPipeList().stream().map((a) -> a.name$() + ": " + (box.datalakeManager().status(a) ? "Running" : "Stopped")).collect(Collectors.toList());
	}
}