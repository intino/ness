package io.intino.ness.triton.datalake;

import io.intino.alexandria.inl.Message;
import io.intino.alexandria.jms.MessageFactory;
import io.intino.alexandria.jms.TopicProducer;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.zim.ZimReader;
import io.intino.ness.core.functions.MessageFunction;
import io.intino.ness.core.functions.MessageMapper;
import io.intino.ness.triton.bus.BusManager;
import io.intino.ness.triton.graph.Pipe;

import static io.intino.alexandria.jms.Consumer.textFrom;


public class PipeStarter {

	private BusManager bus;

	public PipeStarter(BusManager manager) {
		this.bus = manager;
	}

	public void start(Pipe pipe) {
		start(pipe.origin(), pipe.destination(), pipe.transformer());
	}

	private void start(String origin, String destination, MessageFunction function) {
		bus.registerConsumer(origin, message -> pipeTo(destination, textFrom(message), function));
	}

	private void pipeTo(String destination, String message, MessageFunction function) {
		try {
			String toSend = transform(message, function);
			if (toSend != null && !toSend.isEmpty()) send(destination, toSend);
		} catch (Throwable e) {
			Logger.error(e.getMessage(), e);
		}
	}

	private String transform(String message, MessageFunction function) {
		String toSend = message;
		if (function != null) {
			final Message transformed = transform(new ZimReader(message).next(), function);
			toSend = transformed != null ? transformed.toString() : "";
		}
		return toSend;
	}

	private void send(String destination, String toSend) {
		final TopicProducer producer = bus.getTopicProducer(destination);
		new Thread(() -> send(producer, toSend)).start();
	}

	private void send(TopicProducer producer, String finalToSend) {
		if (producer != null) producer.produce(MessageFactory.createMessageFor(finalToSend));
	}

	private Message transform(Message message, MessageFunction function) {
		return function instanceof MessageMapper ? ((MessageMapper) function).map(message) : null;
	}
}
