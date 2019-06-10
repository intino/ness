package io.intino.ness.triton.bus;

import io.intino.alexandria.jms.MessageFactory;
import io.intino.alexandria.jms.TopicProducer;
import io.intino.alexandria.logger.Logger;
import io.intino.ness.triton.graph.Pipe;

import static io.intino.alexandria.jms.Consumer.textFrom;


public class BusPipe {

	private BusManager bus;

	public BusPipe(BusManager manager) {
		this.bus = manager;
	}

	public void start(Pipe pipe) {
		bus.registerConsumer(pipe.origin(), message -> send(pipe.destination(), textFrom(message)));
		Logger.info("Pipe " + pipe.origin() + " -> " + pipe.destination() + " established");
	}

	//	private String transform(String message, MessageFunction function) {
//		String toSend = message;
//		if (function != null) {
//			final Message transformed = transform(new ZimReader(message).next(), function);
//			toSend = transformed != null ? transformed.toString() : "";
//		}
//		return toSend;
//	}

	private void send(String destination, String message) {
		final TopicProducer producer = bus.getTopicProducer(destination);
		new Thread(() -> send(producer, message)).start();
	}

	private void send(TopicProducer producer, String finalToSend) {
		if (producer != null) producer.produce(MessageFactory.createMessageFor(finalToSend));
	}

//	private Message transform(Message message, MessageFunction function) {
//		return function instanceof MessageMapper ? ((MessageMapper) function).map(message) : null;
//	}
}