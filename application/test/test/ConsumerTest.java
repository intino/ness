package test;

import io.intino.konos.jms.Consumer;
import io.intino.konos.jms.TopicConsumer;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import static javax.jms.Session.AUTO_ACKNOWLEDGE;
import static org.apache.activemq.ActiveMQConnection.makeConnection;

public class ConsumerTest {
	private static final Logger logger = LoggerFactory.getLogger(ConsumerTest.class);
	private String url = "tcp://localhost:63000";
	private String user = "consul";
	private String password = "volk96e3atir";
	private String topic = "flow.cesar.infrastructure.operation";
	private Session session;
	private Connection connection;


	public static void main(String[] args) throws InterruptedException {
		new ConsumerTest().consume();
		Thread.sleep(10000000);
	}

	public ConsumerTest() {
		try {
			setUp();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public ConsumerTest setTopic(String topic) {
		this.topic = topic;
		return this;
	}

	public void consume() {
		try {
			MessageConsumer consumer = this.session.createConsumer(session.createTopic(topic));
			consumer.setMessageListener(m -> System.out.println(Consumer.textFrom(m)));
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	public boolean checkConsume() {
		try {
			final boolean[] checked = {false};
			new TopicConsumer(session, topic).listen(m -> {
				System.out.println("received message from " + topic);
				checked[0] = true;
			});
			Thread.sleep(3000);
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