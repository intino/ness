package io.intino.ness;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.topic.Message;
import com.hazelcast.topic.MessageListener;
import io.intino.ness.master.messages.MasterMessageSerializer;
import io.intino.ness.master.messages.MasterTopics;
import io.intino.ness.master.messages.UpdateMasterMessage;
import io.intino.ness.master.model.Triplet;
import io.intino.ness.master.model.TripletRecord;
import io.intino.ness.master.serialization.MasterSerializers;

import java.time.Instant;

public class MasterClient_ {

	public static void main(String[] args) {

		ClientConfig config = new ClientConfig();
		config.setInstanceName("hz-client");
		config.setNetworkConfig(new ClientNetworkConfig().addAddress("localhost:62555"));

		HazelcastInstance hz = HazelcastClient.newHazelcastClient(config);

		hz.getMap("master").get("2520019:theater");

		hz.getTopic(MasterTopics.MASTER_LISTENER_TOPIC).addMessageListener(new MessageListener<Object>() {
			@Override
			public void onMessage(Message<Object> message) {
				String m = String.valueOf(message.getMessageObject());
				System.out.println(m);
			}
		});

		hz.getTopic(MasterTopics.MASTER_UPDATE_TOPIC).publish(MasterMessageSerializer.serialize(new UpdateMasterMessage(
				config.getInstanceName(),
				UpdateMasterMessage.Action.Publish,
				MasterSerializers.getDefault().serialize(new TripletRecord("2520019:theater")
						.put(new Triplet("2520019:theater", "name", String.valueOf(System.nanoTime())))),
				Instant.now()
		)));

		Runtime.getRuntime().addShutdownHook(new Thread(hz::shutdown));
	}
}
