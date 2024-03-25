import io.intino.alexandria.jms.BrokerConnector;
import io.intino.alexandria.jms.ConnectionConfig;
import io.intino.alexandria.jms.ConnectionListener;
import io.intino.alexandria.logger.Logger;
import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.activemq.command.ActiveMQTopic;

public class ProducerTest {


	public static void main(String[] args) throws JMSException {
		Connection connection = BrokerConnector.createConnection(new ConnectionConfig("failover:(tcp://localhost:62000)", "trooper", "trooper", "trooper-bridge"), connectionListener());
		if (connection == null) return;
		connection.setClientID("test-bridge1");
		connection.start();
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		ActiveMQTopic destination = new ActiveMQTopic("example.topic1");
		MessageProducer producer = session.createProducer(destination);
		ActiveMQTextMessage message = new ActiveMQTextMessage();
		message.setText("Hola Mundo!");
		producer.send(destination, message);
		System.out.println("Sent message!");
		producer.close();
		session.close();
		connection.close();
	}

	private static ConnectionListener connectionListener() {
		return new ConnectionListener() {
			@Override
			public void transportInterupted() {
				Logger.info("Interrupted");

			}

			@Override
			public void transportResumed() {
				Logger.info("Connected");
			}
		};
	}
}
