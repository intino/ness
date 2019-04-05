package io.intino.ness.triton.box.actions;

import io.intino.ness.triton.box.TritonBox;


public class RemoveTopicAction {

	public TritonBox box;
	public String topic;

	public Boolean execute() {
		box.busManager().removeTopic(topic);
		return true;
	}
}