package io.intino.ness.box;

import io.intino.konos.jms.Consumer;
import io.intino.konos.jms.TopicConsumer;
import io.intino.konos.jms.TopicProducer;
import io.intino.ness.box.Main;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;
import java.io.File;
import java.util.logging.Level;

import static java.util.logging.Logger.getGlobal;

public class Ness {

	private final Thread thread;
	private Session session;
	private Connection connection;


	public Ness(File workingDirectory, String nessieToken) {
		this(workingDirectory, nessieToken, 61616);
	}

	public Ness(File workingDirectory, String nessieToken, int busPort) {
		thread = new Thread(() -> Main.main(new String[]{
				"ness.store=" + new File(workingDirectory, "store"),
				"nessie.token=" + nessieToken,
				"ness.rootPath=" + new File(workingDirectory, "ness"),
				"broker.port=" + busPort,
				"broker.store=" + new File(workingDirectory, "broker").getAbsolutePath()
		}));
	}

	public void start() {
		try {
			thread.join();
			connection = new ActiveMQConnectionFactory("vm://ness").createConnection("ness", "ness");
			connection.start();
			this.session = connection.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
		} catch (JMSException e) {
			getGlobal().log(Level.SEVERE, e.getMessage(), e);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void stop() {
		thread.interrupt();
	}

	public javax.jms.Session session() {
		return session;
	}

	public TopicProducer registerProducer(String path) {
		try {
			return new TopicProducer(session(), path);
		} catch (JMSException e) {
			getGlobal().severe(e.getMessage());
			return null;
		}
	}

	public TopicConsumer registerConsumer(String path, Consumer consumer) {
		TopicConsumer topicConsumer = new TopicConsumer(this.session(), path);
		topicConsumer.listen(consumer);
		return topicConsumer;
	}

	public TopicConsumer registerConsumer(String path, Consumer consumer, String subscriberID) {
		TopicConsumer topicConsumer = new TopicConsumer(this.session(), path);
		topicConsumer.listen(consumer, subscriberID);
		return topicConsumer;

	}

	public void closeSession() {
		try {
			session.close();
		} catch (JMSException e) {
			getGlobal().log(Level.SEVERE, e.getMessage(), e);
		}
	}
}
