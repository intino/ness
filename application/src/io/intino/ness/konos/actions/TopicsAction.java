package io.intino.ness.konos.actions;

import io.intino.ness.DatalakeManager;
import io.intino.ness.konos.NessBox;

import java.util.List;


public class TopicsAction {

	public NessBox box;


	public List<String> execute() {
		return box.get(DatalakeManager.class).topics();
	}
}