package io.intino.datahub.broker.jms;

import io.intino.alexandria.Scale;
import io.intino.alexandria.jms.*;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.broker.BrokerService;
import io.intino.datahub.model.Broker;
import io.intino.datahub.model.Broker.CompositeDestination.Type;
import io.intino.datahub.model.Datalake;
import io.intino.datahub.model.NessGraph;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.broker.SslContext;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.broker.region.CompositeDestinationInterceptor;
import org.apache.activemq.broker.region.DestinationInterceptor;
import org.apache.activemq.broker.region.policy.ConstantPendingMessageLimitStrategy;
import org.apache.activemq.broker.region.policy.PolicyEntry;
import org.apache.activemq.broker.region.policy.PolicyMap;
import org.apache.activemq.broker.region.virtual.CompositeDestination;
import org.apache.activemq.broker.region.virtual.CompositeQueue;
import org.apache.activemq.broker.region.virtual.CompositeTopic;
import org.apache.activemq.broker.region.virtual.VirtualDestinationInterceptor;
import org.apache.activemq.broker.util.TimeStampingBrokerPlugin;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.activemq.network.jms.InboundTopicBridge;
import org.apache.activemq.network.jms.SimpleJmsTopicConnector;
import org.apache.activemq.plugin.java.JavaRuntimeConfigurationPlugin;
import org.apache.activemq.security.AuthenticationUser;
import org.apache.activemq.security.SimpleAuthenticationPlugin;
import org.apache.activemq.store.kahadb.KahaDBPersistenceAdapter;

import javax.jms.*;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static io.intino.alexandria.jms.MessageReader.textFrom;
import static javax.jms.Session.AUTO_ACKNOWLEDGE;

public class JmsBrokerService implements BrokerService {
	private static final String NESS = "ness";

	private final DataHubBox box;
	private final File root;
	private final File brokerStage;
	private final SSLConfiguration sslConfiguration;
	private final BrokerManager brokerManager;
	private final PipeManager pipeManager;
	private final Map<String, VirtualDestinationInterceptor> pipes = new HashMap<>();

	private org.apache.activemq.broker.BrokerService service;

	public JmsBrokerService(DataHubBox box, File brokerStage) {
		this(box, brokerStage, null);
	}

	public JmsBrokerService(DataHubBox box, File brokerStage, SSLConfiguration sslConfiguration) {
		this.box = box;
		this.root = new File(box.graph().broker().path());
		this.brokerStage = brokerStage;
		this.sslConfiguration = sslConfiguration;
		configure();
		this.brokerManager = new BrokerManager(box.graph(), new AdvisoryManager(jmsBroker()));
		this.pipeManager = new PipeManager(brokerManager, box.graph().broker().pipeList());
	}

	public void start() {
		try {
			Logger.info("Starting broker...");
			this.service.start();
			this.service.waitUntilStarted();
			this.brokerManager.start();
			this.pipeManager.start();
			Logger.info("Broker started in port " + graph().broker().port());
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}
	}

	public void stop() {
		try {
			if (service != null) {
				pipeManager.stop();
				brokerManager.stop();
				service.stop();
				service.waitUntilStopped();
			}
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}
	}

	@Override
	public io.intino.datahub.broker.BrokerManager manager() {
		return brokerManager;
	}

	private NessGraph graph() {
		return box.graph();
	}

	private void configure() {
		try {
			service = new org.apache.activemq.broker.BrokerService();
			service.setBrokerName(NESS);
			service.setPersistent(true);
			service.setOfflineDurableSubscriberTaskSchedule(86400000);
			service.setOfflineDurableSubscriberTimeout(86400000 * 3);
			service.setPersistenceAdapter(persistenceAdapter());
			service.setDataDirectory(new File(root, "activemq-data").getAbsolutePath());
			service.setRestartAllowed(true);
			service.setUseJmx(true);
			service.setUseShutdownHook(true);
			service.setAdvisorySupport(true);
			service.setSchedulePeriodForDestinationPurge(86400000);
			service.setPlugins(new BrokerPlugin[]{new SimpleAuthenticationPlugin(registerUsers()), new JavaRuntimeConfigurationPlugin(), new TimeStampingBrokerPlugin()});
			List<CompositeDestinationInterceptor> destinationInterceptors = new ArrayList<>();
			for (Broker.CompositeDestination o : box.graph().broker().compositeDestinationList()) {
				CompositeDestination composite = o.type().equals(Type.Topic) ? new CompositeTopic() : new CompositeQueue();
				composite.setForwardTo(o.forwardTo().stream().map(f -> o.type().equals(Type.Topic) ? new ActiveMQTopic(f) : new ActiveMQQueue(f)).toList());
				destinationInterceptors.add(new CompositeDestinationInterceptor(new DestinationInterceptor[]{composite}));
			}
			service.setDestinationInterceptors(destinationInterceptors.toArray(DestinationInterceptor[]::new));
			addPolicies();
			if (sslConfiguration != null) addSSLConnector();
			else {
				addTCPConnector();
				addMQTTConnector();
			}
			graph().broker().bridgeList().forEach(this::addJmsBridge);
		} catch (Exception e) {
			Logger.error("Error configuring: " + e.getMessage(), e);
		}
	}

	private List<AuthenticationUser> registerUsers() {
		ArrayList<AuthenticationUser> users = new ArrayList<>();
		users.add(new AuthenticationUser(NESS, NESS, "admin"));
		for (Broker.User user : graph().broker().userList())
			users.add(new AuthenticationUser(user.name(), user.password(), "users"));
		return users;
	}

	public Map<String, VirtualDestinationInterceptor> pipes() {
		return pipes;
	}

	private org.apache.activemq.broker.Broker jmsBroker() {
		try {
			return service.getBroker();
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
			return null;
		}
	}

	private InboundTopicBridge[] toInboundBridges(List<String> inboundTopics) {
		return inboundTopics.stream().map(topicName -> {
			InboundTopicBridge bridge = new InboundTopicBridge(topicName);
			bridge.setLocalTopicName(topicName);
			return bridge;
		}).toArray(InboundTopicBridge[]::new);
	}

	private void addJmsBridge(Broker.Bridge c) {
		try {
			SimpleJmsTopicConnector connector = new SimpleJmsTopicConnector();
			connector.setName(c.name$());
			connector.setLocalTopicConnectionFactory(new ActiveMQConnectionFactory(NESS, NESS, "vm://ness?waitForStart=1000&create=false"));
			connector.setOutboundTopicConnectionFactory(new ActiveMQConnectionFactory(c.externalBus().user(), c.externalBus().password(), c.externalBus().url()));
			connector.setOutboundUsername(c.externalBus().user());
			connector.setOutboundPassword(c.externalBus().password());
			connector.setInboundTopicBridges(toInboundBridges(c.topics()));
			service.addJmsConnector(connector);
			Logger.info("Connector with " + c.externalBus().url() + " started");
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}
	}

	private void addPolicies() {
		final List<PolicyEntry> policyEntries = new ArrayList<>();
		policyEntries.add(pendingMessagesPolicy());
		policyEntries.add(gcOldQueues());
		final PolicyMap policyMap = new PolicyMap();
		policyMap.setPolicyEntries(policyEntries);
		service.setDestinationPolicy(policyMap);
	}

	private PolicyEntry gcOldQueues() {
		final PolicyEntry entry = new PolicyEntry();
		entry.setQueue(">");
		entry.setGcInactiveDestinations(true);
		entry.setInactiveTimeoutBeforeGC(86400000 / 2);
		return entry;
	}

	private static PolicyEntry pendingMessagesPolicy() {
		final PolicyEntry entry = new PolicyEntry();
		entry.setAdvisoryForDiscardingMessages(true);
		entry.setTopicPrefetch(1);
		entry.setTopic(">");
		ConstantPendingMessageLimitStrategy pendingMessageLimitStrategy = new ConstantPendingMessageLimitStrategy();
		pendingMessageLimitStrategy.setLimit(1000000);
		entry.setPendingMessageLimitStrategy(pendingMessageLimitStrategy);
		return entry;
	}

	private void addTCPConnector() throws Exception {
		TransportConnector connector = new TransportConnector();
		connector.setUri(new URI("tcp://0.0.0.0:" + graph().broker().port() + "?transport.useKeepAlive=true"));
		connector.setName("OWireConn");
		service.addConnector(connector);
	}

	private void addSSLConnector() throws Exception {
		TransportConnector connector = new TransportConnector();
		connector.setUri(new URI("ssl://0.0.0.0:" + graph().broker().port() + "?transport.useKeepAlive=true&amp;needClientAuth=true"));
		connector.setName("ssl");
		configureSSL();
		service.addConnector(connector);
	}

	private void configureSSL() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException {
		KeyStore keyStore = KeyStore.getInstance("JKS");
		keyStore.load(new FileInputStream(sslConfiguration.keyStore()), sslConfiguration.keyStorePassword());
		KeyStore trustStore = KeyStore.getInstance("JKS");
		trustStore.load(new FileInputStream(sslConfiguration.trustStore()), sslConfiguration.trustStorePassword());

		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
		keyManagerFactory.init(keyStore, sslConfiguration.keyStorePassword());

		TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
		trustManagerFactory.init(trustStore);
		service.setSslContext(new SslContext(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null));

	}

	private void addMQTTConnector() throws Exception {
		if (graph().broker().secondaryPort() == 0) return;
		TransportConnector mqtt = new TransportConnector();
		mqtt.setUri(new URI("mqtt://0.0.0.0:" + graph().broker().secondaryPort()));
		mqtt.setName("MQTTConn");
		service.addConnector(mqtt);
	}

	private KahaDBPersistenceAdapter persistenceAdapter() {
		KahaDBPersistenceAdapter adapter = new KahaDBPersistenceAdapter();
		adapter.setDirectoryArchive(new File(root, "archive"));
		adapter.setIndexDirectory(new File(root, "entries"));
		adapter.setDirectory(root);
		adapter.setBrokerName(NESS);
		adapter.setBrokerService(service);
		return adapter;
	}

	private ActiveMQTextMessage createMessage(String message) throws MessageNotWriteableException {
		ActiveMQTextMessage textMessage = new ActiveMQTextMessage();
		textMessage.setText(message);
		return textMessage;
	}

	final class BrokerManager implements io.intino.datahub.broker.BrokerManager {
		private final Map<String, JmsProducer> producers = new HashMap<>();
		private final Map<String, List<JmsConsumer>> consumers = new HashMap<>();
		private final NessGraph graph;
		private final AdvisoryManager advisoryManager;
		private Connection connection;
		private Session session;

		BrokerManager(NessGraph graph, AdvisoryManager advisoryManager) {
			this.graph = graph;
			this.advisoryManager = advisoryManager;
		}

		void start() {
			startNessSession();
			initTankConsumers();
			Datalake.ProcessStatus processStatus = graph.datalake().processStatus();
			if (processStatus != null) registerProcessStatus(datalakeScale(), processStatus);
		}

		void stop() {
			try {
				Logger.info("Stopping bus");
				consumers.values().forEach(c -> c.forEach(JmsConsumer::close));
				consumers.clear();
				producers.values().forEach(JmsProducer::close);
				producers.clear();
				session.close();
//			connection.stop();
				session = null;
				connection = null;
				service.stop();
				Logger.info("bus stopped");
			} catch (Throwable e) {
				Logger.error(e);
			}
		}

		public Session session() {
			return session;
		}

		public TopicConsumer registerTopicConsumer(String topic, Consumer<Message> consumer) {
			List<JmsConsumer> list = new ArrayList<>();
			if (!this.consumers.containsKey(topic)) this.consumers.putIfAbsent(topic, list);
			else list = this.consumers.get(topic);
			try {
				TopicConsumer topicConsumer = new TopicConsumer(nessSession(), topic);
				topicConsumer.listen(consumer);
				list.add(topicConsumer);
				return topicConsumer;
			} catch (JMSException e) {
				Logger.error(e);
			}
			return null;
		}

		public QueueConsumer registerQueueConsumer(String topic, Consumer<Message> consumer) {
			List<JmsConsumer> list = new ArrayList<>();
			if (!this.consumers.containsKey(topic)) this.consumers.putIfAbsent(topic, list);
			else list = this.consumers.get(topic);
			try {
				QueueConsumer queueConsumer = new QueueConsumer(nessSession(), topic);
				queueConsumer.listen(consumer);
				list.add(queueConsumer);
				return queueConsumer;
			} catch (JMSException e) {
				Logger.error(e);
			}
			return null;
		}

		public void unregisterConsumer(TopicConsumer consumer) {
			consumer.close();
			consumers.values().forEach(list -> list.remove(consumer));
		}

		@Override
		public void unregisterQueueProducer(String destination) {
			JmsProducer producer = producers.get(destination);
			if (producer != null && !producer.isClosed()) producer.close();
			producers.remove(destination);
		}

		public QueueProducer queueProducerOf(String queue) {
			try {
				if (!this.producers.containsKey(queue))
					this.producers.put(queue, new QueueProducer(nessSession(), queue));
				return (QueueProducer) this.producers.get(queue);
			} catch (JMSException e) {
				Logger.error(e.getMessage(), e);
				return null;
			}
		}

		public TopicProducer topicProducerOf(String topic) {
			try {
				if (!this.producers.containsKey(topic))
					this.producers.put(topic, new TopicProducer(nessSession(), topic));
				return (TopicProducer) this.producers.get(topic);
			} catch (JMSException e) {
				Logger.error(e.getMessage(), e);
				return null;
			}
		}

		void stopConsumersOf(String topic) {
			if (!this.consumers.containsKey(topic)) return;
			this.consumers.get(topic).forEach(JmsConsumer::close);
			this.consumers.get(topic).clear();
		}

		private void startNessSession() {
			try {
				connection = new ActiveMQConnectionFactory("vm://" + "ness").createConnection("ness", "ness");
				connection.setClientID("ness");
				session = connection.createSession(false, AUTO_ACKNOWLEDGE);
				advisoryManager.start(session);
				connection.start();
			} catch (JMSException e) {
				Logger.error(e.getMessage(), e);
			}
		}

		private void initTankConsumers() {
			if (graph.datalake() == null) return;
			brokerStage.mkdirs();
			graph.datalake().tankList().forEach(this::registerTankConsumer);
			Logger.info("Tanks ignited!");
		}

		private void registerTankConsumer(Datalake.Tank t) {
			brokerManager.registerTopicConsumer(t.qn(), new JmsMessageSerializer(brokerStage, t, scale(t), box.datamarts()).create());
		}

		private void registerProcessStatus(Scale scale, Datalake.ProcessStatus ps) {
			brokerManager.registerTopicConsumer(ps.name(), new ProcessStatusSerializer(brokerStage, ps.name(), scale).create());
		}

		private Scale scale(Datalake.Tank t) {
			return t.scale() != null ? Scale.valueOf(t.scale().name()) : datalakeScale();
		}

		private Scale datalakeScale() {
			return Scale.valueOf(graph.datalake().scale().name());
		}

		private Session nessSession() {
			if (this.session == null || closedSession()) startNessSession();
			return session;
		}

		private boolean closedSession() {
			return ((ActiveMQSession) session).isClosed();
		}
	}

	private class PipeManager {
		private final List<Broker.Pipe> pipes;
		private final BrokerManager brokerManager;

		PipeManager(BrokerManager manager, List<Broker.Pipe> pipes) {
			this.brokerManager = manager;
			this.pipes = pipes;
		}

		void start() {
			for (Broker.Pipe pipe : pipes) {
				brokerManager.registerTopicConsumer(pipe.origin(), message -> send(pipe.destination(), textFrom(message)));
				Logger.info("Pipe " + pipe.origin() + " -> " + pipe.destination() + " established");
			}
		}

		void stop() {
			for (Broker.Pipe pipe : pipes) {
				brokerManager.stopConsumersOf(pipe.origin());
				Logger.info("Pipe " + pipe.origin() + " -> " + pipe.destination() + " established");
			}
		}

		private void send(String destination, String message) {
			final TopicProducer producer = brokerManager.topicProducerOf(destination);
			new Thread(() -> send(producer, message)).start();
		}

		private void send(TopicProducer producer, String message) {
			if (producer != null) {
				try {
					producer.produce(createMessage(message));
				} catch (MessageNotWriteableException e) {
					Logger.error(e);
				}
			}
		}
	}
}
