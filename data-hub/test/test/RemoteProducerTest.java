package test;

import io.intino.alexandria.jms.TopicProducer;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.Message;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.jms.Connection;
import javax.jms.Session;
import java.time.Instant;
import java.util.Random;

import static io.intino.alexandria.jms.MessageFactory.createMessageFor;
import static java.lang.Thread.sleep;
import static javax.jms.Session.AUTO_ACKNOWLEDGE;
import static org.apache.activemq.ActiveMQConnection.makeConnection;

public class RemoteProducerTest {
	private final String url = "tcp://bus.siani.es:61616";
	private final String user = "cesar";
	private final String password = "cesar";
	private final String topic = "feed.cesar.infrastructure.operation";

	private Session session;
	private Connection connection;
	private TopicProducer topicProducer;
	private Random random;

	public RemoteProducerTest() {
		initSession();
	}

	@Test
	@Ignore
	public void produce() {
		try {
			while (true) {
				produceMessage();
				sleep(500);
			}
		} catch (Exception ignored) {
		}
	}

	public void produceMessage() {
		final String value = new Message("example.message").set("ts", Instant.now().toString()).set("value", random.nextInt()).toString();
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
			this.connection = makeConnection(user, password, url);
			this.connection.start();
			this.session = connection.createSession(false, AUTO_ACKNOWLEDGE);
			this.topicProducer = new TopicProducer(session, topic);
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}
	}
}
