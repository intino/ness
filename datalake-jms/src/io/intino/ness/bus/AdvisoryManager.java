package io.intino.ness.bus;

import org.apache.activemq.advisory.AdvisorySupport;
import org.apache.activemq.broker.Broker;
import org.apache.activemq.command.ActiveMQDestination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class AdvisoryManager {
	private static final Logger logger = LoggerFactory.getLogger(BusManager.class);

	private final Broker broker;
	private final Session session;
	private HashMap<String, Info> info = new HashMap<>();

	public AdvisoryManager(Broker broker, Session session) {
		this.broker = broker;
		this.session = session;
	}

	public void start() {
		try {
			List<ActiveMQDestination> destinations = destinations();
			for (ActiveMQDestination destination : destinations) {
				info.put(destination.getPhysicalName(), new Info());
				Destination advisory = session.createTopic(AdvisorySupport.getConsumerAdvisoryTopic(destination).getPhysicalName());
				MessageConsumer consumer = session.createConsumer(advisory);
				consumer.setMessageListener(message -> {
					Info info = this.info.get(destination.getPhysicalName());
					info.consumers = consumers(message);
					info.producers = producers(message);
				});
			}
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	private List<ActiveMQDestination> destinations() {
		try {
			return Arrays.stream(broker.getDestinations()).filter(n -> !n.getPhysicalName().contains("ActiveMQ.Advisory")).collect(Collectors.toList());
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	public List<String> topicsInfo() {
		try {
			List<ActiveMQDestination> destinations = Arrays.stream(broker.getDestinations()).filter(d -> !d.getPhysicalName().contains("ActiveMQ.Advisory")).collect(Collectors.toList());
			return destinations.stream().map((d) ->
					d.getPhysicalName() + " Consumers:" + consumersOf(d) + " Producers:" + producersOf(d) + " Enqueued:" + enqueuedMessageOf(d) +
							" Enqueued:" + dequeuedMessageOf(d)).collect(Collectors.toList());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return Collections.emptyList();
		}
	}

	public int consumersOf(ActiveMQDestination d) {
		return info.get(d.getPhysicalName()) == null ? 0 : info.get(d.getPhysicalName()).consumers;
	}


	public int producersOf(ActiveMQDestination d) {
		return info.get(d.getPhysicalName()) == null ? 0 : info.get(d.getPhysicalName()).producers;
	}

	public int enqueuedMessageOf(ActiveMQDestination d) {
		return 0;
	}

	public int dequeuedMessageOf(ActiveMQDestination d) {
		return 0;
	}

	private int consumers(Message message) {
		try {
			return message.getIntProperty("consumerCount");
		} catch (JMSException | NumberFormatException e) {
			return 0;
		}
	}

	private int producers(Message message) {
		try {
			return message.getIntProperty("producerCount");
		} catch (JMSException | NumberFormatException e) {
			return 0;
		}
	}

	private class Info {
		int consumers = 0;
		int producers = 0;
		int enqueued = 0;
		int dequeued = 0;
	}
}