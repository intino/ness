package io.intino.ness.datalake;

import io.intino.konos.jms.MessageFactory;
import io.intino.konos.jms.TopicProducer;
import io.intino.ness.Inl;
import io.intino.ness.bus.BusManager;
import io.intino.ness.graph.Function;
import io.intino.ness.graph.Pipe;
import io.intino.ness.graph.Tank;
import io.intino.ness.inl.Message;
import io.intino.ness.inl.MessageMapper;
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

	private void start(Tank tankFrom, Tank tankTo, Function function) {
		bus.registerConsumer(tankFrom.feedQN(), message -> pipeTo(tankTo, Inl.load(textFrom(message)).get(0), function));
	}

	private Message transform(Message message, Function function) {
		return function.aClass() instanceof MessageMapper ? ((MessageMapper) function.aClass()).map(message) : null;
	}

	private void pipeTo(Tank tankTo, Message message, Function function) {
		try {
			final Message transformed = function == null ? message : transform(message, function);
			if (transformed != null) {
				final TopicProducer producer = bus.getProducer(tankTo.feedQN());
				new Thread(() -> {
					if (producer != null)
						producer.produce(MessageFactory.createMessageFor(transformed.toString()));
				}).start();
			}
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
	}
}
