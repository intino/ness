
package io.intino.ness.bus;

import io.intino.konos.jms.Consumer;
import io.intino.konos.jms.Producer;
import io.intino.konos.jms.TopicConsumer;
import io.intino.konos.jms.TopicProducer;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.command.ActiveMQDestination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;
import java.util.*;

import static javax.jms.Session.AUTO_ACKNOWLEDGE;
import static javax.jms.Session.SESSION_TRANSACTED;

public final class BusManager {
	private static final Logger logger = LoggerFactory.getLogger(BusManager.class);
	private static final String NESS = "ness";

	private final String nessID;
	private final BusService service;
	private final Map<String, TopicProducer> producers = new HashMap<>();
	private final Map<String, List<TopicConsumer>> consumers = new HashMap<>();
	private Connection connection;
	private Session session;
	private AdvisoryManager advisoryManager;


	public BusManager(String nessID, BusService service) {
		this.nessID = nessID;
		this.service = service;
	}

	public void start() {
		service.start();
		initNessSession();
		logger.info("JMS service: started!");
	}

	public String nessID() {
		return nessID;
	}

	public Session nessSession() {
		if (this.session == null || closedSession()) initNessSession();
		return session;
	}

	public void removeTopic(String topic) {
		service.removeTopic(service.findTopic(topic));
	}

	public boolean renameTopic(String topic, String newName) {
		ActiveMQDestination destination = service.findTopic(topic);
		if (destination == null) return false;
		destination.setPhysicalName(newName);
		return true;
	}

	public ActiveMQDestination getOrCreateTopic(String name) {
		try {
			final Session session = nessSession();
			final ActiveMQDestination topic = service.findTopic(name);
			return topic == null ? (ActiveMQDestination) session.createTopic(name) : topic;
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	public ActiveMQDestination createQueue(String name) {
		try {
			return (ActiveMQDestination) nessSession().createQueue(name);
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	public void registerConsumer(String topic, Consumer consumer) {
		List<TopicConsumer> value = new ArrayList<>();
		if (!this.consumers.containsKey(topic)) this.consumers.put(topic, value);
		else value = this.consumers.get(topic);
		TopicConsumer topicConsumer = new TopicConsumer(nessSession(), topic);
		topicConsumer.listen(consumer);
		value.add(topicConsumer);
	}

	public void registerConsumer(String topic, Consumer consumer, String id) {
		List<TopicConsumer> value = new ArrayList<>();
		if (!this.consumers.containsKey(topic)) this.consumers.put(topic, value);
		else value = this.consumers.get(topic);
		TopicConsumer topicConsumer = new TopicConsumer(nessSession(), topic);
		topicConsumer.listen(consumer, id);
		value.add(topicConsumer);
	}

	private boolean closedSession() {
		return ((ActiveMQSession) session).isClosed();
	}

	public List<TopicConsumer> consumersOf(String feedQN) {
		return consumers.getOrDefault(feedQN, Collections.emptyList());
	}

	public void stop() {
		try {
			logger.info("Stopping bus");
			consumers.values().forEach(c -> c.forEach(TopicConsumer::stop));
			consumers.clear();
			producers.values().forEach(Producer::close);
			producers.clear();
			session.close();
			connection.close();
			service.stop();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void startAdvisories() throws JMSException {
		advisoryManager = new AdvisoryManager(service.broker(), session);
		advisoryManager.start();
	}

	private void initNessSession() {
		try {
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://" + NESS);
			connection = connectionFactory.createConnection(NESS, NESS);
			connection.setClientID(nessID);
			session = connection.createSession(false, AUTO_ACKNOWLEDGE);
			startAdvisories();
			connection.start();
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public Session transactedSession() {
		try {
			if (((ActiveMQConnection) connection).isTransportFailed()) initNessSession();
			return connection.createSession(true, SESSION_TRANSACTED);
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	public List<String> topicsInfo() {
		return advisoryManager.topicsInfo();
	}

	public void pipe(String feed, String flow) {
		service.pipe(feed, flow);
		service.updateInterceptors();
	}

	public void stopPipe(String feed, String flow) {
		service.stopPipe(feed, flow);
		service.updateInterceptors();
	}
}
