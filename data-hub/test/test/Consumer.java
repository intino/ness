package test;

import io.intino.alexandria.jms.TopicConsumer;
import io.intino.alexandria.logger.Logger;
import io.intino.ness.datahub.datalake.MessageTranslator;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.JMSException;
import javax.jms.Session;

import static javax.jms.Session.AUTO_ACKNOWLEDGE;

public class Consumer {

	public static void main(String[] args) {
		start(new TopicConsumer(sessionLocal(), "feed.consul.serverstatus"));
	}

	private static void start(TopicConsumer consumer) {
		System.out.println("connected");
		consumer.listen(message -> {
			try {
				System.out.println(MessageTranslator.toInlMessage(message).toString());
			} catch (Throwable e) {
				Logger.error(e);
			}
		});
	}

	private static Session sessionLocal() {
		try {
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("failover:(tcp://cesar.ces.siani.es:63000)");
			javax.jms.Connection connection = connectionFactory.createConnection("cesar", "vs9]DJZzMl");
			connection.start();
			return connection.createSession(false, AUTO_ACKNOWLEDGE);
		} catch (JMSException e) {
			Logger.error(e.getMessage(), e);
			return null;
		}
	}
}
