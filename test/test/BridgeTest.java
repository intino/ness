import io.intino.alexandria.jms.BrokerConnector;
import io.intino.alexandria.jms.ConnectionConfig;
import io.intino.alexandria.jms.ConnectionListener;
import io.intino.alexandria.logger.Logger;
import jakarta.jms.*;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.log4j.Level;

public class BridgeTest {


	public static void main(String[] args) throws JMSException {
		io.intino.alexandria.logger4j.Logger.init(Level.TRACE);
		Connection connection = BrokerConnector.createConnection(new ConnectionConfig("failover:(tcp://localhost:63000)", "trooper", "trooper", "test-bridge2"), connectionListener());
		if (connection == null) return;
		connection.setClientID("test-bridge2");
		connection.start();
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		ActiveMQTopic topic = new ActiveMQTopic("example.topic1");
		MessageConsumer subscriber = session.createDurableSubscriber(topic, "bridge-listener", null, false);
		System.out.println("Listening");
		subscriber.setMessageListener(m -> {
			try {
				System.out.println(((TextMessage) m).getText());
			} catch (JMSException e) {
				Logger.error(e);
			}
		});
	}

	private static ConnectionListener connectionListener() {
		return new ConnectionListener() {
			@Override
			public void transportInterupted() {
				Logger.info("Interrpted");

			}

			@Override
			public void transportResumed() {
				Logger.info("Connected");
			}
		};
	}
}
