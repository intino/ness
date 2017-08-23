package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;

import java.util.Collections;
import java.util.List;


public class TopicsAction {

	public NessBox box;


	public List<String> execute() {
		List<String> list = box.busManager().topicsInfo();
		Collections.sort(list);
		return list;
	}
}