package io.intino.ness.datalake;

import io.intino.konos.alexandria.Inl;
import io.intino.konos.alexandria.functions.MessageMapper;
import io.intino.konos.jms.MessageFactory;
import io.intino.konos.jms.TopicProducer;
import io.intino.ness.bus.BusManager;
import io.intino.ness.graph.Function;
import io.intino.ness.graph.Pipe;
import io.intino.ness.graph.Tank;
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
		start(pipe.isTankSource() ? pipe.asTankSource().origin().feedQN() : pipe.asTopicSource().origin(), pipe.destination(), pipe.transformer());
	}

	private void start(String tankFrom, Tank tankTo, Function function) {
		bus.registerConsumer(tankFrom, message -> pipeTo(tankTo, textFrom(message), function));
	}

	private void pipeTo(Tank tankTo, String message, Function function) {
		try {
			String toSend = message;
			if (function != null) {
				final Message transformed = transform(Inl.load(message).get(0), function);
				toSend = transformed != null ? transformed.toString() : "";
			}
			if (toSend != null && !toSend.isEmpty()) {
				final TopicProducer producer = bus.getProducer(tankTo.feedQN());
				String finalToSend = toSend;
				new Thread(() -> send(producer, finalToSend)).start();
			}
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void send(TopicProducer producer, String finalToSend) {
		if (producer != null) producer.produce(MessageFactory.createMessageFor(finalToSend));
	}

	private Message transform(Message message, Function function) {
		return function.aClass() instanceof MessageMapper ? ((MessageMapper) function.aClass()).map(message) : null;
	}
}
