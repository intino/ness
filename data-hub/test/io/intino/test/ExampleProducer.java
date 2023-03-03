package io.intino.test;

import io.intino.alexandria.jms.TopicProducer;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.Message;
import io.intino.alexandria.message.MessageReader;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageNotWriteableException;
import javax.jms.Session;
import java.time.Instant;

import static javax.jms.Session.AUTO_ACKNOWLEDGE;
import static org.apache.activemq.ActiveMQConnection.makeConnection;

public class ExampleProducer {
	private final String url = "tcp://localhost:63000";
	private final String user = "consul";
	private final String password = "consul";
	private final String topic = "it.Mode";

	private Session session;
	private Connection connection;
	private TopicProducer topicProducer;

	public ExampleProducer() {
		initSession();
	}

	public static void main(String[] args) throws JMSException {
		new ExampleProducer().produceMessage();
		System.out.println(message().toString());

	}

	public void produceMessage() throws JMSException {
		final Message message = message();
		topicProducer.produce(createMessage(message.toString()));
		session.close();
		connection.close();
	}

	private static Message message() {
		return new MessageReader("[Mode]\n" +
				"ss: test\n" +
				"value: On\n" +
				"ts: " + Instant.now().toString()).iterator().next();
	}

	private ActiveMQTextMessage createMessage(String message) throws MessageNotWriteableException {
		ActiveMQTextMessage textMessage = new ActiveMQTextMessage();
		textMessage.setText(message);
		return textMessage;
	}

	private void initSession() {
		try {
			this.connection = makeConnection(user, password, url);
			this.connection.start();
			this.session = connection.createSession(false, AUTO_ACKNOWLEDGE);
			this.topicProducer = new TopicProducer(session, topic);
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}
	}
}
