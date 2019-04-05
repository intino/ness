package io.intino.ness.box.actions;

import io.intino.ness.box.NessServiceBox;

import java.util.Collections;
import java.util.List;


public class TopicsAction {

	public NessServiceBox box;


	public List<String> execute() {
		List<String> list = box.busManager().topicsInfo();
		Collections.sort(list);
		return list;
	}
}