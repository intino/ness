package io.intino.ness.bus;

import io.intino.konos.jms.TopicConsumer;
import io.intino.ness.graph.BusPipe;
import io.intino.ness.graph.ExternalBus;
import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.DestinationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import java.util.*;
import java.util.regex.Pattern;

import static io.intino.ness.bus.MessageSender.send;
import static io.intino.ness.graph.BusPipe.Direction.incoming;
import static java.lang.Thread.sleep;
import static java.util.stream.Collectors.toList;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;

public class BusPipeManager {
	private static final Logger logger = LoggerFactory.getLogger(ROOT_LOGGER_NAME);
	private final BusPipe busPipe;
	private final List<TopicConsumer> internalConsumers;
	private final BusManager busManager;
	private final List<String> targetDestinations;
	private final ExternalBus externalBus;

	public BusPipeManager(BusPipe busPipe, BusManager busManager) {
		this.busPipe = busPipe;
		this.busManager = busManager;
		this.internalConsumers = new ArrayList<>();
		this.externalBus = busPipe.bus();
		this.externalBus.initSession(busManager.nessID());
		List<String> externalBusTopics = filter(externalBusTopics(), busPipe.tankMacro());
		this.targetDestinations = busPipe.direction().equals(incoming) ?
				filter(externalBusTopics, busPipe.tankMacro()) :
				filter(merge(busManager.topics(), externalBusTopics), busPipe.tankMacro());
	}

	public void start() {
		if (busPipe.direction().equals(incoming)) incomingPipe();
		else outgoingPipe();
	}

	public void stop() {
		externalBus.close();
		internalConsumers.forEach(TopicConsumer::stop);
		internalConsumers.clear();
		externalBus.close();
	}

	private void incomingPipe() {
		for (String topic : targetDestinations) {
			TopicConsumer consumer = externalBus.addConsumer(topic);
			consumer.listen(m -> incomingMessage(topic, m).start(), busManager.nessID() + "-" + topic);
		}
	}

	private void outgoingPipe() {
		for (String topic : targetDestinations) {
			TopicConsumer consumer = new TopicConsumer(busManager.nessSession(), topic);
			internalConsumers.add(consumer);
			consumer.listen(m -> outgoingMessage(topic, m).start());
		}
	}

	private Thread incomingMessage(String topic, Message m) {
		return new Thread(() -> send(busManager.nessSession(), topic, m, busPipe.transformer()));
	}

	private Thread outgoingMessage(String topic, Message m) {
		return new Thread(() -> {
			checkSession();
			send(externalBus.session(), topic, m, busPipe.transformer());
		});
	}

	private List<String> filter(Collection<String> topics, String macro) {
		Pattern pattern = Pattern.compile(macro);
		return topics.stream().filter(t -> pattern.matcher(t).matches()).collect(toList());
	}

	private Collection<String> externalBusTopics() {
		Set<String> topics = new HashSet<>();
		try {
			checkSession();
			MessageConsumer consumer = externalBus.addConsumer(externalBus.createTopic("ActiveMQ.Advisory.Topic"));
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

	private void checkSession() {
		if (externalBus.session() == null || ((ActiveMQSession) externalBus.session()).isClosed()) {
			externalBus.initSession(busManager.nessID());
			incomingPipe();
		}
	}

	private List<String> merge(List<String> topics, List<String> externalBusTopics) {
		HashSet<String> objects = new HashSet<>(topics);
		objects.addAll(externalBusTopics);
		return new ArrayList<>(objects);
	}
}
