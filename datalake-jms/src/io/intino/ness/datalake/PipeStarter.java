package io.intino.ness.datalake;

import io.intino.konos.alexandria.Inl;
import io.intino.konos.alexandria.functions.MessageFunction;
import io.intino.konos.alexandria.functions.MessageMapper;
import io.intino.konos.jms.MessageFactory;
import io.intino.konos.jms.TopicProducer;
import io.intino.ness.bus.BusManager;
import io.intino.ness.graph.Pipe;
import io.intino.ness.inl.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.intino.konos.jms.Consumer.textFrom;

public class PipeStarter {
	private static final Logger logger = LoggerFactory.getLogger(PipeStarter.class);

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
			logger.error(e.getMessage(), e);
		}
	}

	private String transform(String message, MessageFunction function) {
		String toSend = message;
		if (function != null) {
			final Message transformed = transform(Inl.load(message).get(0), function);
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
