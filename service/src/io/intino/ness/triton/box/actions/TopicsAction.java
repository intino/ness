package io.intino.ness.triton.box.actions;

import io.intino.ness.triton.box.ServiceBox;

import java.util.Collections;
import java.util.List;


public class TopicsAction {

	public ServiceBox box;


	public List<String> execute() {
		List<String> list = box.busManager().topicsInfo();
		Collections.sort(list);
		return list;
	}
}