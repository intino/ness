import io.intino.alexandria.inl.MessageBuilder;
import io.intino.alexandria.jms.TopicProducer;
import io.intino.alexandria.logger.Logger;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.JMSException;
import javax.jms.Session;
import java.time.Instant;

import static io.intino.alexandria.jms.MessageFactory.createMessageFor;
import static java.lang.Thread.sleep;
import static javax.jms.Session.AUTO_ACKNOWLEDGE;

public class Feeder {

	public static void main(String[] args) throws JMSException {
		Session session = sessionLocal();
		start(new TopicProducer(session, "feed.consul.serverstatus"));
	}

	private static void start(TopicProducer producer) {
		new Thread(() -> {
			while (true)
				try {
					producer.produce(createMessageFor(message()));
					System.out.println("sent!");
					sleep(1000);
				} catch (InterruptedException ignored) {
				}
		}).start();
	}

	private static String message() {
		return MessageBuilder.toMessage(new ExampleMessage(java.util.UUID.randomUUID().toString(), Math.random() * 60)).toString();
	}

	private static Session sessionLocal() {
		try {
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:63000");
			javax.jms.Connection connection = connectionFactory.createConnection("test", "test");
			connection.start();
			return connection.createSession(false, AUTO_ACKNOWLEDGE);
		} catch (JMSException e) {
			Logger.error(e.getMessage(), e);
			return null;
		}
	}

	public static class ExampleMessage {
		Instant ts = Instant.now();
		String user;
		double value;

		public ExampleMessage(String user, double value) {
			this.user = user;
			this.value = value;
		}
	}
}
