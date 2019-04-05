package io.intino.ness.box.actions;

import io.intino.ness.box.NessServiceBox;


public class RemoveTopicAction {

	public NessServiceBox box;
	public String topic;

	public Boolean execute() {
		box.busManager().removeTopic(topic);
		return true;
	}
}