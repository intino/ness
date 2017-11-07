package io.intino.ness.graph;

import io.intino.konos.jms.TopicConsumer;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.DestinationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.*;
import java.util.*;

import static java.lang.Thread.sleep;
import static javax.jms.Session.AUTO_ACKNOWLEDGE;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;

public class ExternalBus extends AbstractExternalBus {
	private static final Logger logger = LoggerFactory.getLogger(ROOT_LOGGER_NAME);
	private Connection connection;
	private Session session;
	private Map<String, TopicConsumer> consumers = new HashMap<>();

	public ExternalBus(io.intino.tara.magritte.Node node) {
		super(node);
	}


	public Session initSession(String id) {
		setSession(id);
		return session != null && !((ActiveMQSession) session).isClosed() ? session : reload();
	}

	public synchronized Session reload() {
		try {
			cleanOldSession();
			logger.info("session with " + originURL + " reloaded");
			ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(originURL());
			factory.setClientID(sessionID);
			if (connection == null || ((ActiveMQConnection) this.connection).isClosed())
				this.connection = factory.createConnection(user(), password());
			this.session = connection.createSession(false, AUTO_ACKNOWLEDGE);
			connection.start();
			return session;
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	public synchronized Session session() {
		return session;
	}

	public TopicConsumer addConsumer(String topic) {
		TopicConsumer topicConsumer = new TopicConsumer(session(), topic);
		consumers.put(topic, topicConsumer);
		return topicConsumer;
	}

	public MessageConsumer addConsumer(Topic topic) {
		try {
			return session().createConsumer(topic);
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	public Topic createTopic(String topic) {
		try {
			return session().createTopic(topic);
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	public void close() {
		try {
			consumers.values().forEach(TopicConsumer::stop);
			consumers.clear();
			session().close();
			connection.close();
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
		}
	}


	public Collection<String> topics() {
		Set<String> topics = new HashSet<>();
		try {
			if (sessionIsClosed()) reload();
			MessageConsumer consumer = addConsumer(createTopic("ActiveMQ.Advisory.Topic"));
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

	public boolean sessionIsClosed() {
		return session != null && ((ActiveMQSession) session).isClosed();
	}

	private void setSession(String id) {
		if (sessionID == null) {
			sessionID(id);
			save$();
		}
	}

	private void cleanOldSession() {
		for (TopicConsumer consumer : consumers.values())
			if (consumer != null) consumer.stop();
		for (String c : consumers.keySet()) consumers.put(c, null);
		if (session != null) try {
			session.close();
		} catch (JMSException e) {
		}
	}

	public Map<String, TopicConsumer> consumers() {
		return consumers;
	}
}