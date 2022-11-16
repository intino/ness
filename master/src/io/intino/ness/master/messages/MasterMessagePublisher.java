package io.intino.ness.master.messages;

import io.intino.ness.master.core.Master;

public class MasterMessagePublisher {

	public static void publishMessage(Master master, String topic, MasterMessage message) {
		master.hazelcast().getTopic(topic).publish(MasterMessageSerializer.serialize(message));
	}
}
