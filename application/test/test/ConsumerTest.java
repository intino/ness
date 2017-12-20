package test;

import io.intino.konos.jms.TopicConsumer;
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

public class ConsumerTest {
	private static final Logger logger = LoggerFactory.getLogger(ConsumerTest.class);
	private static String url = "tcp://localhost:63000";
	private static String user = "consul";
	private static String password = "volk96e3atir";
	private Session session;
	private Connection connection;

	private String topic = "flow.cesar.infrastructure.operation";

	public ConsumerTest() {
		try {
			setUp();
		} catch (Exception e) {
		}
	}

	@Test
	public void consume() throws Exception {
		new TopicConsumer(session, topic).listen((m) -> System.out.println(textFrom(m)));
		sleep(1000000);
	}


	public boolean checkConsume() {
		try {
			final boolean[] checked = {false};
			new TopicConsumer(session, topic).listen(m -> checked[0] = true);
			Thread.sleep(4000);
			return checked[0];
		} catch (InterruptedException e) {
			return false;
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