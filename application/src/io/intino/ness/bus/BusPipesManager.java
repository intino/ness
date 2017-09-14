package io.intino.ness.bus;

import io.intino.konos.jms.TopicConsumer;
import io.intino.ness.graph.Aqueduct;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.DestinationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import java.util.*;
import java.util.regex.Pattern;

import static io.intino.ness.bus.MessageSender.send;
import static io.intino.ness.graph.Aqueduct.Direction.incoming;
import static java.lang.Thread.sleep;
import static java.util.stream.Collectors.toList;
import static javax.jms.Session.AUTO_ACKNOWLEDGE;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;

public class BusPipesManager {
	private static final Logger logger = LoggerFactory.getLogger(ROOT_LOGGER_NAME);
	private final Aqueduct aqueduct;
	private Session externalBus;
	private final List<String> nessTopics;
	private final List<TopicConsumer> topicConsumers;
	private final BusManager busManager;

	public BusPipesManager(Aqueduct aqueduct, BusManager busManager) {
		this.aqueduct = aqueduct;
		this.busManager = busManager;
		this.nessTopics = busManager.topics();
		this.topicConsumers = new ArrayList<>();
		initForeignSession();
	}

	public void start() {
		if (aqueduct.direction().equals(incoming)) for (String topic : filter(externalBusTopics(), aqueduct.tankMacro())) {
			TopicConsumer consumer = new TopicConsumer(externalBus, topic);
			consumer.listen(m -> send(busManager.nessSession(), topic, m, aqueduct.transformer()));
			topicConsumers.add(consumer);
		}
		else for (String topic : filter(nessTopics, aqueduct.tankMacro())) {
			TopicConsumer consumer = new TopicConsumer(busManager.nessSession(), topic);
			consumer.listen(m -> {
				if (externalBus == null || ((ActiveMQSession) externalBus).isClosed()) initForeignSession();
				send(externalBus, topic, m, aqueduct.transformer());
			});
			topicConsumers.add(consumer);
		}
	}

	private Collection<String> filter(Collection<String> topics, String macro) {
		Pattern pattern = Pattern.compile(macro);
		return topics.stream().filter(t -> pattern.matcher(t).matches()).collect(toList());
	}

	public void stop() {
		try {
			TopicConsumer consumer = null;
			for (TopicConsumer topicConsumer : topicConsumers) {
				consumer = topicConsumer;
				topicConsumer.stop();
			}
			if (consumer != null) topicConsumers.remove(consumer);
			externalBus.close();
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void initForeignSession() {
		try {
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(aqueduct.bus().originURL());
			javax.jms.Connection connection = connectionFactory.createConnection(aqueduct.bus().user(), aqueduct.bus().password());
			externalBus = connection.createSession(false, AUTO_ACKNOWLEDGE);
			connection.start();
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private Collection<String> externalBusTopics() {
		if (externalBus == null || ((ActiveMQSession) externalBus).isClosed()) initForeignSession();
		Set<String> topics = new HashSet<>();
		try {
			MessageConsumer consumer = externalBus.createConsumer(externalBus.createTopic("ActiveMQ.Advisory.Topic"));
			consumer.setMessageListener(message -> {
				ActiveMQMessage m = (ActiveMQMessage) message;
				if (m.getDataStructure() instanceof DestinationInfo)
					topics.add(((DestinationInfo) m.getDataStructure()).getDestination().getPhysicalName());
			});
			sleep(3000);
			consumer.close();
		} catch (JMSException | InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
		return filter(topics, aqueduct.tankMacro());
	}
}
