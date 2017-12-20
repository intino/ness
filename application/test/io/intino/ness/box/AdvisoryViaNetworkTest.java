package io.intino.ness.box;

import io.intino.konos.jms.Consumer;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ConnectionClosedException;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.region.DestinationInterceptor;
import org.apache.activemq.broker.region.virtual.CompositeTopic;
import org.apache.activemq.broker.region.virtual.VirtualDestination;
import org.apache.activemq.broker.region.virtual.VirtualDestinationInterceptor;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.activemq.util.IdGenerator;

import javax.jms.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AdvisoryViaNetworkTest {
	private static final String AUTO_ASSIGN_TRANSPORT = "tcp://localhost:55056";
	private static int maxSetupTime = 5000;
	private static BrokerItem brokerItem;


	public static void main(String[] args) throws Exception {
		brokerItem = createBroker("A");
		ActiveMQTopic advisoryQueue = new ActiveMQTopic("advQ");

		// convert advisories into advQ that cross the network bridge
		CompositeTopic compositeTopic = new CompositeTopic();
		compositeTopic.setName("topicA");
		compositeTopic.setForwardOnly(false);
		compositeTopic.setForwardTo(Arrays.asList(advisoryQueue));
		VirtualDestinationInterceptor virtualDestinationInterceptor = new VirtualDestinationInterceptor();
		virtualDestinationInterceptor.setVirtualDestinations(new VirtualDestination[]{compositeTopic});
		brokerItem.broker.setDestinationInterceptors(new DestinationInterceptor[]{virtualDestinationInterceptor});

		startAllBrokers();

		final MessageConsumer consumer = createConsumer(new ActiveMQTopic("advQ"));
		if (consumer== null) return;
		consumer.setMessageListener(message -> System.out.println(Consumer.textFrom(message)));
		Thread.sleep(1000000);
	}

	private static BrokerItem createBroker(String brokerName) throws Exception {
		final BrokerService broker = new BrokerService();
		broker.setPersistent(false);
		broker.setUseJmx(false);
		broker.setBrokerName(brokerName);
		broker.addConnector(new URI(AUTO_ASSIGN_TRANSPORT));
		return new BrokerItem(broker);
	}

	private static MessageConsumer createConsumer(Destination dest) throws Exception {
		if (brokerItem != null) return brokerItem.createConsumer(dest);
		return null;
	}

	private static void startAllBrokers() throws Exception {
		BrokerService broker = brokerItem.broker;
		broker.start();
		broker.waitUntilStarted();
		Thread.sleep(maxSetupTime);
	}


	public static class BrokerItem {

		public BrokerService broker;
		public ActiveMQConnectionFactory factory;
		public List<Connection> connections;
		public List<MessageConsumer> consumers;
		public boolean persistent;
		private IdGenerator id;

		public BrokerItem(BrokerService broker) throws Exception {
			this.broker = broker;

			factory = new ActiveMQConnectionFactory(broker.getVmConnectorURI());
			factory.setConnectionIDPrefix(broker.getBrokerName());
			consumers = Collections.synchronizedList(new ArrayList<MessageConsumer>());
			connections = Collections.synchronizedList(new ArrayList<Connection>());
			id = new IdGenerator(broker.getBrokerName() + ":");
		}

		public Connection createConnection() throws Exception {
			Connection conn = factory.createConnection();
			conn.setClientID(id.generateId());

			connections.add(conn);
			return conn;
		}

		public MessageConsumer createConsumer(Destination dest) throws Exception {
			Connection c = createConnection();
			c.start();
			Session s = c.createSession(false, Session.AUTO_ACKNOWLEDGE);
			final MessageConsumer consumer = s.createConsumer(dest);
			consumers.add(consumer);
			return consumer;
		}

		public MessageConsumer createDurableSubscriber(Topic dest, String name) throws Exception {
			Connection c = createConnection();
			c.start();
			Session s = c.createSession(false, Session.AUTO_ACKNOWLEDGE);
			return createDurableSubscriber(dest, s, name);
		}

		public MessageConsumer createDurableSubscriber(Topic dest, Session sess, String name) throws Exception {
			MessageConsumer client = sess.createDurableSubscriber(dest, name);
			consumers.add(client);
			return client;
		}


		public MessageProducer createProducer(Destination dest) throws Exception {
			Connection c = createConnection();
			c.start();
			Session s = c.createSession(false, Session.AUTO_ACKNOWLEDGE);
			return createProducer(dest, s);
		}

		public MessageProducer createProducer(Destination dest, Session sess) throws Exception {
			MessageProducer client = sess.createProducer(dest);
			client.setDeliveryMode(persistent ? DeliveryMode.PERSISTENT : DeliveryMode.NON_PERSISTENT);
			return client;
		}

		public void destroy() throws Exception {
			while (!connections.isEmpty()) {
				Connection c = connections.remove(0);
				try {
					c.close();
				} catch (ConnectionClosedException e) {
				} catch (JMSException e) {
				}
			}

			broker.stop();
			broker.waitUntilStopped();
			consumers.clear();

			broker = null;
			connections = null;
			consumers = null;
			factory = null;
		}
	}
}
