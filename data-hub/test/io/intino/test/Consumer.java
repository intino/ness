package io.intino.test;

import io.intino.alexandria.jms.TopicConsumer;
import io.intino.alexandria.logger.Logger;
import jakarta.jms.JMSException;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import org.apache.activemq.ActiveMQConnectionFactory;

import static jakarta.jms.Session.AUTO_ACKNOWLEDGE;

public class Consumer {

	public static void main(String[] args) throws JMSException {
		start(new TopicConsumer(sessionLocal(), "service.ness.seal"));
	}

	private static void start(TopicConsumer consumer) {
		System.out.println("connected!");
		consumer.listen(message -> {
			try {
				System.out.println(((TextMessage) message).getText());
			} catch (Throwable e) {
				Logger.error(e);
			}
		});
	}

	private static Session sessionLocal() {
		try {
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("failover:(tcp://localhost:63000)");
			jakarta.jms.Connection connection = connectionFactory.createConnection("cobel", "cobel");
			connection.start();
			return connection.createSession(false, AUTO_ACKNOWLEDGE);
		} catch (JMSException e) {
			Logger.error(e.getMessage(), e);
			return null;
		}
	}
}
