package io.intino.ness.konos;

import io.intino.konos.jms.TopicProducer;
import io.intino.ness.Inl;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.JMSException;
import javax.jms.Session;
import java.time.Instant;

import static io.intino.konos.jms.MessageFactory.createMessageFor;
import static java.lang.Thread.sleep;
import static java.util.logging.Logger.getGlobal;
import static javax.jms.Session.AUTO_ACKNOWLEDGE;

public class Feeder {


	public static void main(String[] args) throws JMSException, InterruptedException {
		TopicProducer producer = new TopicProducer(session(), "feed.tank.weather.Temperature.1");
		new Thread(() -> {
			while (true)
				try {
					producer.produce(createMessageFor(message()));
					sleep(1000);
				} catch (InterruptedException ignored) {
				}
		}).start();
	}

	private static String message() {
		return Inl.serialize(new ExampleMessage(java.util.UUID.randomUUID().toString(), "temperature"));
	}

	public static class ExampleMessage {
		Instant ts = Instant.now();
		String user;
		String operation;

		public ExampleMessage(String user, String operation) {
			this.user = user;
			this.operation = operation;
		}
	}

	private static Session session() {
		try {
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
			javax.jms.Connection connection = connectionFactory.createConnection("octavioroncal", "octavioroncal");
			connection.setClientID("octavioroncal");
			connection.start();
			return connection.createSession(false, AUTO_ACKNOWLEDGE);
		} catch (JMSException e) {
			getGlobal().severe(e.getMessage());
			return null;
		}
	}
}
