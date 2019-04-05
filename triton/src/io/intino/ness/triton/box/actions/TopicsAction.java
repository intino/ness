package io.intino.ness.triton.box.actions;

import io.intino.ness.triton.box.TritonBox;

import java.util.Collections;
import java.util.List;


public class TopicsAction {

	public TritonBox box;


	public List<String> execute() {
		List<String> list = box.busManager().topicsInfo();
		Collections.sort(list);
		return list;
	}
}