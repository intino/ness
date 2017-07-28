package io.intino.ness.box;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.DestinationInfo;

import javax.jms.*;
import java.util.HashMap;

import static java.lang.Thread.sleep;
import static java.util.logging.Logger.getGlobal;
import static javax.jms.Session.AUTO_ACKNOWLEDGE;

public class Consumer {

	private static HashMap<String, String> clientsMap;
	private static Connection connection;

	public static void main(String[] args) throws JMSException, InterruptedException {
		StringBuilder builder = new StringBuilder();

		Session session = session();
		MessageConsumer consumer = session.createConsumer(session.createTopic("ActiveMQ.Advisory.Topic"));
		consumer.setMessageListener(message -> {
			ActiveMQMessage m = (ActiveMQMessage) message;
			if (m.getDataStructure() instanceof DestinationInfo) {
				String physicalName = ((DestinationInfo) m.getDataStructure()).getDestination().getPhysicalName();
				builder.append(physicalName).append("\n");
			}
		});
		sleep(1000);
		consumer.close();
		session.close();
		System.out.println(builder.toString());
		connection.close();
	}


	private static Session session() {
		try {
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://bus.pre.monentia.es:62616");
			connection = connectionFactory.createConnection("happysense.sumus", "mol5:inquire");
			connection.start();
			return connection.createSession(false, AUTO_ACKNOWLEDGE);
		} catch (JMSException e) {
			getGlobal().severe(e.getMessage());
			return null;
		}
	}

}
