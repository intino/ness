package io.intino.ness.bus;

import io.intino.konos.jms.TopicConsumer;
import io.intino.ness.graph.BusPipe;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.DestinationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import java.util.*;
import java.util.regex.Pattern;

import static io.intino.ness.bus.MessageSender.send;
import static io.intino.ness.graph.BusPipe.Direction.incoming;
import static java.lang.Thread.sleep;
import static java.util.stream.Collectors.toList;
import static javax.jms.Session.AUTO_ACKNOWLEDGE;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;

public class BusPipeManager {
	private static final Logger logger = LoggerFactory.getLogger(ROOT_LOGGER_NAME);
	private final BusPipe aqueduct;
	private Session externalBus;
	private final List<String> nessTopics;
	private final List<TopicConsumer> topicConsumers;
	private final BusManager busManager;
	private Connection externalBusConnection;

	public BusPipeManager(BusPipe aqueduct, BusManager busManager) {
		this.aqueduct = aqueduct;
		this.busManager = busManager;
		this.nessTopics = busManager.topics();
		this.topicConsumers = new ArrayList<>();
		initForeignSession();
	}

	public void start() {
		if (aqueduct.direction().equals(incoming)) {
			Collection<String> topics = externalBusTopics();
			Collection<String> filter = filter(topics, aqueduct.tankMacro());
			for (String topic : filter) {
				TopicConsumer consumer = new TopicConsumer(externalBus, topic);
				consumer.listen(m -> new Thread(() -> send(busManager.nessSession(), topic, m, aqueduct.transformer())).start(), "ness." + topic);
				topicConsumers.add(consumer);
			}
		} else for (String topic : filter(nessTopics, aqueduct.tankMacro())) {
			TopicConsumer consumer = new TopicConsumer(busManager.nessSession(), topic);
			consumer.listen(m -> {
				if (externalBus == null || ((ActiveMQSession) externalBus).isClosed()) initForeignSession();
				new Thread(() -> send(externalBus, topic, m, aqueduct.transformer())).start();
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
			externalBus.close();
			externalBusConnection.close();
			topicConsumers.forEach(TopicConsumer::stop);
			topicConsumers.clear();
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void initForeignSession() {
		try {
			ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(aqueduct.bus().originURL());
			factory.setClientID(busManager.nessID() + "-" + aqueduct.name$());
			externalBusConnection = factory.createConnection(aqueduct.bus().user(), aqueduct.bus().password());
			externalBus = externalBusConnection.createSession(false, AUTO_ACKNOWLEDGE);
			externalBusConnection.start();
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private Collection<String> externalBusTopics() {
		Set<String> topics = new HashSet<>();
		try {
			if (externalBus == null || ((ActiveMQSession) externalBus).isClosed()) initForeignSession();
			if (externalBus == null) return topics;
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
		return topics;
	}
}
