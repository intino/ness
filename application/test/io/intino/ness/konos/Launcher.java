package io.intino.ness.konos;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Ignore;
import org.junit.Test;

import javax.jms.*;

public class Launcher {

	public static class Pre {

		public static void main(String[] args) throws Exception {
			Main.main(new String[]{
					"graph.store=./temp/store",
					"nessie.token=xoxb-162074419812-gB5oNUwzxGWQ756TrRyu1Ii9",
					"ness.rootPath=./temp/local",
					"broker.port=61616",
					"broker.store=./temp/broker/"
			});
		}
	}

	@Test
	@Ignore
	public void produce() {
		try {
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
			Connection connection = connectionFactory.createConnection("cesar", "6e4518bj1hpm");
			connection.start();
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Destination destination = session.createTopic("TEST.FOO");
			MessageProducer producer = session.createProducer(destination);
			producer.setDeliveryMode(DeliveryMode.PERSISTENT);
			String text = "Hello world! From: " + Thread.currentThread().getName() + " : " + this.hashCode();
			TextMessage message = session.createTextMessage(text);
			System.out.println("Sent message: " + message.hashCode() + " : " + Thread.currentThread().getName());
			producer.send(message);
			session.close();
			connection.close();
		} catch (Exception e) {
			System.out.println("Caught: " + e);
			e.printStackTrace();
		}
	}
}