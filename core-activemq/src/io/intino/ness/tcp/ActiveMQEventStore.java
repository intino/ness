package io.intino.ness.tcp;

import io.intino.alexandria.inl.Message;
import io.intino.alexandria.jms.TopicConsumer;
import io.intino.alexandria.jms.TopicProducer;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.zim.ZimStream;
import io.intino.alexandria.zim.ZimStream.Merge;
import io.intino.ness.core.Datalake;
import io.intino.ness.tcp.ActiveMQDatalake.Connection;
import org.apache.activemq.ActiveMQSession;

import javax.jms.JMSException;
import javax.jms.Session;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class ActiveMQEventStore implements Datalake.EventStore {
	private final Map<String, ActiveMQEventTank> tanks;
	private final Map<String, TopicConsumer> consumers;
	private final Map<String, TopicProducer> producers;
	private final Connection connection;
	private AdminService adminService;
	private PumpService pumpService;
	private Session session;

	public ActiveMQEventStore(Connection connection) {
		this.connection = connection;
		this.tanks = new HashMap<>();
		this.consumers = new HashMap<>();
		this.producers = new HashMap<>();
	}

	public void open() {
		this.session = connection.session();
		this.adminService = new AdminService(session);
		this.pumpService = new PumpService(session);
	}

	@Override
	public Stream<Tank> tanks() {
		return tanks.values().stream().map(t -> t);
	}

	@Override
	public Tank tank(String name) {
		ActiveMQEventTank tank = new ActiveMQEventTank(name, pumpService);
		tanks.put(name, tank);
		return tank;
	}

	public void feed(String tank, Message... messages) {
		for (Message message : messages) put(message, feedProbe(tank));
	}

	@Override
	public Reflow reflow(Reflow.Filter filter) {
		String response = adminService.request("seal", 30 * 60 * 1000);
		if (!response.equals("sealed")) return null;
		return new Reflow() {
			private ZimStream is = new Merge(tankInputStreams());

			ZimStream tankInputStream(Tank tank) {
				return tank.content(ts -> filter.allow(tank, ts));
			}

			private ZimStream[] tankInputStreams() {
				return tanks()
						.filter(filter::allow)
						.map(this::tankInputStream)
						.toArray(ZimStream[]::new);
			}

			@Override
			public void next(int blockSize, MessageHandler... messageHandlers) {
				new ReflowBlock(is, messageHandlers).reflow(blockSize);
			}
		};
	}

	@Override
	public Subscription subscribe(Tank tank) {
		return (clientId, messageHandlers) -> {
			TopicConsumer topicConsumer = new TopicConsumer(session, flowProbe(tank.name()));
			if (clientId != null) {
				topicConsumer.listen(message -> handle(message, messageHandlers), clientId);
			} else topicConsumer.listen(message -> handle(message, messageHandlers));
			consumers.put(tank.name(), topicConsumer);
		};
	}

	@Override
	public void unsubscribe(Tank tank) {
		TopicConsumer topicConsumer = consumers.get(tank.name());
		if (topicConsumer != null) topicConsumer.stop();
		consumers.remove(tank.name());
	}

	void seal() {
		adminService.send("seal");
	}

	Collection<TopicProducer> producers() {
		return this.producers.values();
	}

	void put(ZimStream stream, String blob) {
		while (stream.hasNext()) put(stream.next(), putProbe(blob));
	}

	private void put(Message message, String topic) {
		if (session == null || ((ActiveMQSession) session).isClosed()) {
			Logger.error("Session closed");
			return;
		}
		producer(topic).produce(JMSMessageTranslator.fromInlMessage(message));
	}

	private void handle(javax.jms.Message message, MessageHandler[] messageHandlers) {
		for (MessageHandler handler : messageHandlers) handler.handle(JMSMessageTranslator.toInlMessage(message));
	}

	private String feedProbe(String name) {
		return "feed." + name;
	}

	private String flowProbe(String name) {
		return "flow." + name;
	}

	private String putProbe(String name) {
		String fingerprint = name.substring(0, name.indexOf("#"));
		return "put." + fingerprint.substring(0, fingerprint.lastIndexOf("-"));
	}

	private TopicProducer producer(String topic) {
		try {
			if (this.producers.containsKey(topic) && !this.producers.get(topic).isClosed()) return this.producers.get(topic);
			this.producers.put(topic, new TopicProducer(session, topic));
			return producers.get(topic);
		} catch (JMSException e) {
			Logger.error(e);
			return null;
		}
	}

	private static class ReflowBlock {
		private final ZimStream is;
		private final MessageHandler[] messageHandlers;

		ReflowBlock(ZimStream is, MessageHandler[] messageHandlers) {
			this.is = is;
			this.messageHandlers = messageHandlers;
		}

		void reflow(int blockSize) {
			terminate(process(blockSize));
		}

		private int process(int messages) {
			int pendingMessages = messages;
			while (is.hasNext() && pendingMessages-- >= 0) {
				Message message = is.next();
				Arrays.stream(messageHandlers).forEach(mh -> mh.handle(message));
			}
			return messages - pendingMessages;
		}

		private void terminate(int reflowedMessages) {
			Arrays.stream(messageHandlers)
					.filter(m -> m instanceof ReflowHandler)
					.map(m -> (ReflowHandler) m)
					.forEach(m -> terminate(m, reflowedMessages));
		}

		private void terminate(ReflowHandler reflowHandler, int reflowedMessages) {
			if (is.hasNext()) reflowHandler.onBlock(reflowedMessages);
			else reflowHandler.onFinish(reflowedMessages);
		}
	}

}
