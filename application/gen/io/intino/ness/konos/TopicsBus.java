package io.intino.ness.konos;

import io.intino.konos.jms.Bus;
import io.intino.konos.jms.Consumer;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.advisory.DestinationListener;
import org.apache.activemq.command.ActiveMQTopic;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TopicsBus extends Bus {
	private static Logger logger = Logger.getGlobal();

	private NessConfiguration configuration;
	private NessBox box;

	public TopicsBus(NessBox box) {
		this.box = box;
		this.configuration = box.configuration();
		NessConfiguration.TopicsConfiguration busConfiguration = this.configuration.topicsConfiguration();
		try {
			connection = new org.apache.activemq.ActiveMQConnectionFactory(busConfiguration.user, busConfiguration.password, busConfiguration.url).createConnection();
			if (busConfiguration.clientID != null && !busConfiguration.clientID.isEmpty()) connection.setClientID(busConfiguration.clientID);
			connection.start();
			this.session = connection.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);

		} catch (JMSException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	public List<String> topics() {
		List<String> topics = new ArrayList<>();
		try {
			for (ActiveMQTopic topic : ((ActiveMQConnection) connection).getDestinationSource().getTopics())
				topics.add(topic.getTopicName());
		} catch (JMSException e) {
		}
		return topics;
	}

	public void setListener(DestinationListener listener) {
		try {
			((ActiveMQConnection) connection).getDestinationSource().setDestinationListener(listener);
		} catch (JMSException e) {
		}
	}

	private static class Subscriptor implements Consumer {

		private NessBox box;

		Subscriptor(NessBox box) {
			this.box = box;
		}

		public void consume(Message message) {
			String text = textFrom(message);
			String type = typeOf(text);

		}
	}
}