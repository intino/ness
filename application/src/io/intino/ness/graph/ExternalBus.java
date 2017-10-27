package io.intino.ness.graph;

import io.intino.konos.jms.TopicConsumer;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.*;
import java.util.ArrayList;
import java.util.List;

import static javax.jms.Session.AUTO_ACKNOWLEDGE;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;

public class ExternalBus extends AbstractExternalBus {
	private static final Logger logger = LoggerFactory.getLogger(ROOT_LOGGER_NAME);

	private Connection connection;
	private Session session;
	private List<TopicConsumer> consumers = new ArrayList<>();

	public ExternalBus(io.intino.tara.magritte.Node node) {
		super(node);
	}


	public Session initSession(String id) {
		try {
			if (session != null && !((ActiveMQSession) session).isClosed()) return session;
			setSession(id);
			cleanOldSession();
			logger.info("session with " + originURL + " reloaded");
			ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(originURL());
			factory.setClientID(id);
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

	public Session session() {
		return session == null ? initSession(sessionID()) : session;
	}

	public TopicConsumer addConsumer(String topic) {
		TopicConsumer topicConsumer = new TopicConsumer(session(), topic);
		consumers.add(topicConsumer);
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

	private void setSession(String id) {
		if (sessionID == null) {
			sessionID(id);
			save$();
		}
	}

	private void cleanOldSession() {
		for (TopicConsumer consumer : consumers) consumer.stop();
		consumers.clear();
		if (session != null) try {
			session.close();
		} catch (JMSException e) {
		}
	}

	public void close() {
		try {
			consumers.forEach(TopicConsumer::stop);
			consumers.clear();
			session().close();
			connection.close();
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
		}
	}
}