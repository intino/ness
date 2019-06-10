package io.intino.ness.triton.box.actions;

import io.intino.ness.triton.box.ServiceBox;


public class RemoveTopicAction {

	public ServiceBox box;
	public String topic;

	public Boolean execute() {
		box.busManager().removeTopic(topic);
		return true;
	}
}