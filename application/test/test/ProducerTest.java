package test;

import io.intino.konos.jms.TopicProducer;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;

import static io.intino.konos.jms.MessageFactory.createMessageFor;
import static java.lang.Thread.sleep;
import static javax.jms.Session.AUTO_ACKNOWLEDGE;
import static org.apache.activemq.ActiveMQConnection.makeConnection;

public class ProducerTest {
	private static final Logger logger = LoggerFactory.getLogger(ProducerTest.class);
	private static String url = "tcp://localhost:63000";
	private static String user = "consul";
	private static String password = "volk96e3atir";
	private static String topic = "feed.cesar.infrastructure.operation";

	private Session session;
	private Connection connection;


	@Test
	public void produce() throws Exception {
		while (true) {
			new TopicProducer(session, topic).produce(createMessageFor("text"));
			sleep(1000);
			System.out.println("message sent");
		}
	}

	public void produceMessage() {
		try {
			new TopicProducer(session, topic).produce(createMessageFor("text"));
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public ProducerTest() {
		try {
			setUp();
		} catch (Exception e) {
		}
	}

	@Before
	public void setUp() throws Exception {
		try {
			connection = makeConnection(user, password, url);
			connection.start();
			this.session = connection.createSession(false, AUTO_ACKNOWLEDGE);
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
		}
	}
}
