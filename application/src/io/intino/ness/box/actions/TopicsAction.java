package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;

import java.util.List;


public class TopicsAction {

	public NessBox box;


	public List<String> execute() {
		return box.datalakeManager().topics();
	}
}