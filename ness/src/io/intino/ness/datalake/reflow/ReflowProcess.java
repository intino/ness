package io.intino.ness.datalake.reflow;

import io.intino.alexandria.jms.TopicProducer;
import io.intino.ness.core.Datalake;
import io.intino.ness.core.Datalake.EventStore;
import io.intino.ness.core.Datalake.EventStore.Tank;
import io.intino.ness.core.Scale;
import io.intino.ness.core.Timetag;
import io.intino.ness.datalake.reflow.ReflowProcess.ReflowMessageHandler.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import static io.intino.alexandria.jms.MessageFactory.createMessageFor;
import static io.intino.ness.box.Utils.timetag;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;


class ReflowProcess {
	private static final String FLOW_PATH = "flow.ness.reflow";
	private static Logger logger = LoggerFactory.getLogger(ROOT_LOGGER_NAME);
	private final Datalake datalake;
	private final Scale scale;
	private final ReflowConfiguration configuration;
	private final Session session;
	private final TopicProducer producer;
	private final EventStore.Reflow reflow;
	private int count = 0;

	ReflowProcess(Session session, Datalake datalake, Scale scale, ReflowConfiguration configuration) {
		this.session = session;
		this.datalake = datalake;
		this.scale = scale;
		this.configuration = configuration;
		this.producer = createProducer();
		Map<Tank, Entry<Timetag, Timetag>> timetags = collectTimetags();
		this.reflow = this.datalake.eventStore().reflow(new EventStore.Reflow.Filter() {
			@Override
			public boolean allow(Tank tank) {
				return timetags.containsKey(tank);
			}

			@Override
			public boolean allow(Tank tank, Timetag timetag) {
				return timetags.containsKey(tank);
			}
		});
	}

	void next() {
		reflow.next(configuration.blockSize(), reflowHandler(
				message -> producer.produce(createMessageFor(message.toString())),
				() -> {
					producer.produce(createEndBlockMessage(count));
					commit();
				},
				() -> {
					producer.produce(createEndReflowMessage(count));
					commit();
					close();
				}));
	}

	private Message createEndBlockMessage(int count) {
		return createMessageFor("[endBlock]\ncount: " + count + "\n");
	}

	private Message createEndReflowMessage(int count) {
		return createMessageFor("[endReflow]\ncount: " + count + "\n");
	}

	private void close() {
		try {
			session.close();
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void commit() {
		try {
			session.commit();
			logger.info("Commited " + configuration.blockSize() + " messages");
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);

		}
	}

	private TopicProducer createProducer() {
		try {
			return new TopicProducer(session, FLOW_PATH);
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	private Map<Tank, Entry<Timetag, Timetag>> collectTimetags() {
		Map<Tank, Entry<Timetag, Timetag>> timetags = new HashMap<>();
		for (ReflowConfiguration.Tank tank : configuration.tankList()) {
			Tank fsTank = datalake.eventStore().tank(tank.name());
			if (fsTank == null) continue;
			timetags.put(fsTank, timeRange(timetag(tank.from(), scale), timetag(tank.to(), scale)));
		}
		return timetags;
	}

	private Entry<Timetag, Timetag> timeRange(Timetag from, Timetag to) {
		return new AbstractMap.SimpleEntry<>(from, to);
	}

	private EventStore.MessageHandler reflowHandler(Consumer<io.intino.alexandria.inl.Message> handler, Callback onBlock, Callback onFinish) {
		return new ReflowMessageHandler(handler, onBlock, onFinish);
	}

	public static class ReflowMessageHandler implements EventStore.MessageHandler, EventStore.ReflowHandler {

		private final Consumer<io.intino.alexandria.inl.Message> consumer;
		private final Callback onBlock;
		private final Callback onFinish;

		public ReflowMessageHandler(Consumer<io.intino.alexandria.inl.Message> consumer, Callback onBlock, Callback onFinish) {
			this.consumer = consumer;
			this.onBlock = onBlock;
			this.onFinish = onFinish;
		}

		@Override
		public void handle(io.intino.alexandria.inl.Message message) {
			consumer.accept(message);
		}

		@Override
		public void onBlock(int reflowedMessages) {
			onBlock.execute();
		}

		@Override
		public void onFinish(int reflowedMessages) {
			onFinish.execute();
		}

		interface Callback {
			void execute();
		}
	}
}