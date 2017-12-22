package test;

import io.intino.konos.jms.TopicProducer;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.Session;

import static io.intino.konos.jms.MessageFactory.createMessageFor;
import static java.lang.Thread.sleep;
import static javax.jms.Session.AUTO_ACKNOWLEDGE;
import static org.apache.activemq.ActiveMQConnection.makeConnection;

public class ProducerTest {
	private static final Logger logger = LoggerFactory.getLogger(ProducerTest.class);
	private String url = "tcp://localhost:63000";
	private String user = "consul";
	private String password = "volk96e3atir";
	private String topic = "feed.cesar.infrastructure.operation";

	private Session session;
	private Connection connection;
	private TopicProducer topicProducer;

//	public ProducerTest() {
//		initSession();
//	}


	@Test
	public void produce() {
		try {
			while (true) {
				topicProducer.produce(createMessageFor("text"));
				sleep(1000);
				System.out.println("message sent to " + topic);
			}
		} catch (Exception ignored) {
		}
	}

	@Before
	public void setUp() throws Exception {
		initSession();
	}

	private void initSession() {
		try {
			connection = makeConnection(user, password, url);
			connection.start();
			this.session = connection.createSession(false, AUTO_ACKNOWLEDGE);
			topicProducer = new TopicProducer(session, topic);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}
