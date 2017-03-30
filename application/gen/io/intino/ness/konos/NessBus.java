package io.intino.ness.konos;

import io.intino.ness.konos.ApplicationBox;
import io.intino.ness.konos.ApplicationConfiguration;
import io.intino.konos.jms.TopicConsumer;
import io.intino.konos.jms.TopicProducer;
import io.intino.konos.jms.Consumer;
import io.intino.konos.jms.Bus;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NessBus extends Bus {
	private static Logger logger = Logger.getGlobal();

	private ApplicationConfiguration configuration;
	private ApplicationBox box;

	public NessBus(ApplicationBox box) {
		this.box = box;
		this.configuration = box.configuration();
		ApplicationConfiguration.NessConfiguration busConfiguration = this.configuration.nessConfiguration();
		try {
			connection = new org.apache.activemq.ActiveMQConnectionFactory(busConfiguration.user, busConfiguration.password, busConfiguration.url).createConnection();
			if (busConfiguration.clientID != null && !busConfiguration.clientID.isEmpty()) connection.setClientID(busConfiguration.clientID);
			connection.start();
			this.session = connection.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);

		} catch (JMSException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	private static class Subscriptor implements Consumer {

		private ApplicationBox box;

		Subscriptor(ApplicationBox box) {
			this.box = box;
		}

		public void consume(Message message) {
			String text = textFrom(message);
			String type = typeOf(text);

		}
	}
}