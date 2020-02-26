package io.intino.test;

import io.intino.alexandria.jms.TopicProducer;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.MessageBuilder;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.JMSException;
import javax.jms.MessageNotWriteableException;
import javax.jms.Session;
import java.time.Instant;

import static java.lang.Thread.sleep;
import static javax.jms.Session.AUTO_ACKNOWLEDGE;

public class Feeder {

	public static void main(String[] args) throws JMSException {
		start(new TopicProducer(sessionLocal(), "feed.consul.server.status"));
	}

	private static void start(TopicProducer producer) {
		new Thread(() -> {
			while (true) {
				try {
					producer.produce(createMessage(message()));
					System.out.println("sent!");
				} catch (MessageNotWriteableException e) {
					Logger.error(e);
				}
				try {
					sleep(5000);
				} catch (InterruptedException e) {
					Logger.error(e);
				}
			}
		}).start();
	}

	private static String message() {
		return MessageBuilder.toMessage(new ExampleMessage(java.util.UUID.randomUUID().toString(), Math.random() * 60)).toString();
	}

	private static Session sessionLocal() {
		try {
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:63000");
			javax.jms.Connection connection = connectionFactory.createConnection("io/intino/test", "io/intino/test");
			connection.start();
			return connection.createSession(false, AUTO_ACKNOWLEDGE);
		} catch (JMSException e) {
			Logger.error(e.getMessage(), e);
			return null;
		}
	}

	private static ActiveMQTextMessage createMessage(String message) throws MessageNotWriteableException {
		ActiveMQTextMessage textMessage = new ActiveMQTextMessage();
		textMessage.setText(message);
		return textMessage;
	}

	public static class ExampleMessage {
		Instant ts = Instant.now();
		String user;
		double value;

		public ExampleMessage(String user, double value) {
			this.user = user;
			this.value = value;
		}
	}
}
