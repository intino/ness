package io.intino.ness.master.messages;

import com.hazelcast.core.HazelcastInstance;

public class MasterMessagePublisher {

	public static void publishMessage(HazelcastInstance hz, String topic, MasterMessage message) {
		hz.getTopic(topic).publish(MasterMessageSerializer.serialize(message));
	}
}
