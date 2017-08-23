package io.intino.ness.box.actions;

import io.intino.ness.box.NessBox;


public class RemoveTopicAction {

	public NessBox box;
	public String topic;

	public Boolean execute() {
		return box.busManager().removeTopic(topic);
	}
}