package io.intino.test;

import io.intino.alexandria.jms.TopicProducer;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.Message;
import io.intino.alexandria.message.MessageReader;
import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import jakarta.jms.MessageNotWriteableException;
import jakarta.jms.Session;
import org.apache.activemq.command.ActiveMQTextMessage;

import java.time.Instant;

import static jakarta.jms.Session.AUTO_ACKNOWLEDGE;
import static org.apache.activemq.ActiveMQConnection.makeConnection;

public class CFEProducer {
	private final String url = "tcp://localhost:63000";
	private final String user = "comercial.cuentamaestra";
	private final String password = "mTs5i7HpZSC8";
	private final String topic = "comercial.cuentamaestra.GestionAnticipo";

	private Session session;
	private Connection connection;
	private TopicProducer topicProducer;

	public CFEProducer() {
		initSession();
	}

	public static void main(String[] args) throws JMSException {
		new CFEProducer().produceMessage();
		System.out.println(message().toString());

	}

	public void produceMessage() throws JMSException {
		final Message message = message();
		topicProducer.produce(createMessage(message.toString()));
		session.close();
		connection.close();
	}

	private static Message message() {
		return new MessageReader("[GestionAnticipo]\n" +
				"id: 202003_M744_1\n" +
				"estado: Cancelado\n" +
				"cuenta: M744\n" +
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
