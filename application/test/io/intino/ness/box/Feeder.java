package io.intino.ness.box;

import io.intino.konos.jms.TopicProducer;
import io.intino.ness.Inl;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Session;
import java.time.Instant;

import static io.intino.konos.jms.MessageFactory.createMessageFor;
import static java.lang.Thread.sleep;
import static javax.jms.Session.AUTO_ACKNOWLEDGE;

public class Feeder {

	private static final Logger logger = LoggerFactory.getLogger(Feeder.class);

	public static void main(String[] args) throws JMSException, InterruptedException {
		Session session = sessionPre();
		start(new TopicProducer(session, "feed.ebar.sensors"));
//		start(new TopicProducer(sessionPre(), "feed.tank.weather.Humidity.1"));
	}

	private static void start(TopicProducer producer) {
		new Thread(() -> {
			while (true)
				try {
					producer.produce(createMessageFor(message()));
					System.out.println("sent!");
					sleep(1000);
				} catch (InterruptedException ignored) {
				}
		}).start();
	}

	private static String message() {
		return Inl.serialize(new ExampleMessage(java.util.UUID.randomUUID().toString(), Math.random() * 60));
	}

	public static class ExampleMessage {
		Instant created = Instant.now();
		String user;
		double value;

		public ExampleMessage(String user, double value) {
			this.user = user;
			this.value = value;
		}
	}

	static Session sessionPre() {
		try {
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://bus.pre.monentia.es:62616");
			javax.jms.Connection connection = connectionFactory.createConnection("cesar", "ged1*buckers");
			connection.start();
			return connection.createSession(false, AUTO_ACKNOWLEDGE);
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	static Session nessSession() {
		try {
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
			javax.jms.Connection connection = connectionFactory.createConnection("cesar", "5d1pks0m4rp6");
			connection.start();
			return connection.createSession(false, AUTO_ACKNOWLEDGE);
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}
}
