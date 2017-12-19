package io.intino.ness.box;

import io.intino.konos.jms.MessageFactory;
import io.intino.konos.jms.TopicConsumer;
import io.intino.konos.jms.TopicProducer;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;

import static io.intino.konos.jms.Consumer.textFrom;
import static java.lang.Thread.sleep;
import static javax.jms.Session.AUTO_ACKNOWLEDGE;
import static org.apache.activemq.ActiveMQConnection.makeConnection;

public class ProducerTest {
	private static final Logger logger = LoggerFactory.getLogger(ProducerTest.class);
	private static String url = "tcp://localhost:63000";
	private static String user = "example";
	private static String password = "1234";
	private Session session;
	private Connection connection;

	private String topic = "feed.example.1";


	@Test
	public void produce() throws Exception {
		new TopicProducer(session, topic).produce(MessageFactory.createMessageFor("text"));
		new TopicConsumer(session, topic).listen((m) -> System.out.println(textFrom(m)));
		sleep(1000000);
	}

	@Before
	public void setUp() throws Exception {
		try {
			connection = makeConnection(url, user, password);
			connection.start();
			this.session = connection.createSession(false, AUTO_ACKNOWLEDGE);
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
		}
	}
}
