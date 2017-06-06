package io.intino.ness.box;

import io.intino.konos.jms.TopicConsumer;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import static java.util.logging.Logger.getGlobal;
import static javax.jms.Session.AUTO_ACKNOWLEDGE;

public class Consumer {

	public static void main(String[] args) throws JMSException, InterruptedException {
		TopicConsumer consumer = new TopicConsumer(session(), "feed.tank.weather.Temperature.1");
		consumer.listen(new io.intino.konos.jms.Consumer() {
			@Override
			public void consume(Message message) {
				System.out.println(textFrom(message));
			}
		});
	}


	private static Session session() {
		try {
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
			javax.jms.Connection connection = connectionFactory.createConnection("octavioroncal", "octavioroncal");
			connection.start();
			connection.setClientID("octavioroncal");
			return connection.createSession(false, AUTO_ACKNOWLEDGE);
		} catch (JMSException e) {
			getGlobal().severe(e.getMessage());
			return null;
		}
	}

}
