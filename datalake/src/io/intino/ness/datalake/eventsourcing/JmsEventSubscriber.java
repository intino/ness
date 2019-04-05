package io.intino.ness.datalake.eventsourcing;

import io.intino.alexandria.jms.TopicConsumer;
import io.intino.ness.datalake.Datalake.EventStore.Tank;
import io.intino.ness.datalake.eventsourcing.EventPump.EventHandler;

import java.util.HashMap;
import java.util.Map;

public class JmsEventSubscriber implements EventSubscriber {
	private final JmsConnection connection;
	private final Map<String, TopicConsumer> consumers;

	public JmsEventSubscriber(JmsConnection connection) {
		this.connection = connection;
		consumers = new HashMap<>();
	}

	@Override
	public Connection connection() {
		return null;
	}

	@Override
	public Subscription subscribe(Tank tank) {
		return (clientId, eventHandlers) -> {
			TopicConsumer topicConsumer = new TopicConsumer(connection.session(), flowProbe(tank.name()));
			if (clientId != null) {
				topicConsumer.listen(message -> handle(message, eventHandlers), clientId);
			} else topicConsumer.listen(message -> handle(message, eventHandlers));
			consumers.put(tank.name(), topicConsumer);
		};
	}

	@Override
	public void unsubscribe(Tank tank) {
		TopicConsumer topicConsumer = consumers.get(tank.name());
		if (topicConsumer != null) topicConsumer.stop();
	}

	private void handle(javax.jms.Message message, EventHandler[] messageHandlers) {
		for (EventHandler handler : messageHandlers) handler.handle(JmsMessageTranslator.toInlMessage(message));
	}

	private String flowProbe(String name) {
		return "flow." + name;
	}
}
