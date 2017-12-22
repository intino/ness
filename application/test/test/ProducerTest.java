package test;

import io.intino.konos.jms.TopicProducer;
import io.intino.ness.inl.Message;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.Session;
import java.time.Instant;
import java.util.Random;

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
	private Random random;

	@Test
	@Ignore
	public void produce() {
		try {
			while (true) {
				produceMessage();
				sleep(1000);
			}
		} catch (Exception ignored) {
		}
	}

	private void produceMessage() {
		final String value = new Message("example.message").write("ts", Instant.now().toString()).write("value", random.nextInt()).toString();
		topicProducer.produce(createMessageFor(value));
		System.out.println("message sent to " + topic + " ->  " + value);
	}

	@Before
	public void setUp() throws Exception {
		initSession();
		random = new Random(2123132);
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
