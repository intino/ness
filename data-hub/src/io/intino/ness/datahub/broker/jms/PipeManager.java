package io.intino.ness.datahub.broker.jms;

import io.intino.alexandria.jms.MessageFactory;
import io.intino.alexandria.jms.TopicProducer;
import io.intino.alexandria.logger.Logger;
import io.intino.ness.datahub.graph.Broker;

import static io.intino.alexandria.jms.MessageReader.textFrom;


public class PipeManager {

	private BrokerManager brokerManager;


	public PipeManager(BrokerManager manager) {
		this.brokerManager = manager;
	}

	public void start(Broker.Pipe pipe) {
		brokerManager.registerConsumer(pipe.origin(), message -> send(pipe.destination(), textFrom(message)));
		Logger.info("Pipe " + pipe.origin() + " -> " + pipe.destination() + " established");
	}

	private void send(String destination, String message) {
		final TopicProducer producer = brokerManager.getTopicProducer(destination);
		new Thread(() -> send(producer, message)).start();
	}

	private void send(TopicProducer producer, String finalToSend) {
		if (producer != null) producer.produce(MessageFactory.createMessageFor(finalToSend));
	}
}
