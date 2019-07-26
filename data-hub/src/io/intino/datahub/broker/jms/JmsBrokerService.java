package io.intino.datahub.broker.jms;

import io.intino.alexandria.Scale;
import io.intino.alexandria.jms.*;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.broker.BrokerService;
import io.intino.datahub.graph.Broker;
import io.intino.datahub.graph.Datalake;
import io.intino.datahub.graph.NessGraph;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.broker.region.policy.ConstantPendingMessageLimitStrategy;
import org.apache.activemq.broker.region.policy.PolicyEntry;
import org.apache.activemq.broker.region.policy.PolicyMap;
import org.apache.activemq.broker.region.virtual.VirtualDestinationInterceptor;
import org.apache.activemq.network.jms.InboundTopicBridge;
import org.apache.activemq.network.jms.SimpleJmsTopicConnector;
import org.apache.activemq.plugin.java.JavaRuntimeConfigurationPlugin;
import org.apache.activemq.security.AuthenticationUser;
import org.apache.activemq.security.SimpleAuthenticationPlugin;
import org.apache.activemq.store.kahadb.KahaDBPersistenceAdapter;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.intino.alexandria.jms.MessageReader.textFrom;
import static javax.jms.Session.AUTO_ACKNOWLEDGE;

public class JmsBrokerService implements BrokerService {
	private static final String NESS = "ness";
	private final File root;
	private final NessGraph graph;
	private final BrokerManager brokerManager;
	private final PipeManager pipeManager;
	private final Map<String, VirtualDestinationInterceptor> pipes = new HashMap<>();

	private org.apache.activemq.broker.BrokerService service;

	public JmsBrokerService(NessGraph graph) {
		this.root = new File(graph.broker().path());
		this.graph = graph;
		configure();
		this.brokerManager = new BrokerManager(graph, new AdvisoryManager(jmsBroker()));
		this.pipeManager = new PipeManager(brokerManager, graph.broker().pipeList());
	}

	public void start() {
		try {
			Logger.info("Starting Jms broker...");
			this.service.start();
			this.service.waitUntilStarted();
			this.brokerManager.start();
			this.pipeManager.start();
			Logger.info("Jms Broker started!");
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

	private void configure() {
		try {
			service = new org.apache.activemq.broker.BrokerService();
			service.setBrokerName(NESS);
			service.setPersistent(true);
			service.setPersistenceAdapter(persistenceAdapter());
			service.setDataDirectory(new File(root, "activemq-data").getAbsolutePath());
			service.setRestartAllowed(true);
			service.setUseJmx(true);
			service.setUseShutdownHook(true);
			service.setAdvisorySupport(true);
			service.setPlugins(new BrokerPlugin[]{new SimpleAuthenticationPlugin(registerUsers()), new JavaRuntimeConfigurationPlugin()});
			addPolicies();
			addTCPConnector();
			addMQTTConnector();
			graph.broker().bridgeList().forEach(this::addJmsBridge);
		} catch (Exception e) {
			Logger.error("Error configuring: " + e.getMessage(), e);
		}
	}

	private List<AuthenticationUser> registerUsers() {
		ArrayList<AuthenticationUser> users = new ArrayList<>();
		users.add(new AuthenticationUser(NESS, NESS, "admin"));
		for (Broker.User user : graph.broker().userList())
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
		final PolicyEntry entry = new PolicyEntry();
		entry.setAdvisoryForDiscardingMessages(true);
		entry.setTopicPrefetch(1);
		ConstantPendingMessageLimitStrategy pendingMessageLimitStrategy = new ConstantPendingMessageLimitStrategy();
		pendingMessageLimitStrategy.setLimit(1000000);
		entry.setPendingMessageLimitStrategy(pendingMessageLimitStrategy);
		final PolicyMap policyMap = new PolicyMap();
		policyMap.setPolicyEntries(policyEntries);
		service.setDestinationPolicy(policyMap);
	}

	private void addTCPConnector() throws Exception {
		TransportConnector connector = new TransportConnector();
		connector.setUri(new URI("tcp://0.0.0.0:" + graph.broker().port() + "?transport.useKeepAlive=true"));
		connector.setName("OWireConn");
		service.addConnector(connector);
	}

	private void addMQTTConnector() throws Exception {
		if (graph.broker().secondaryPort() == 0) return;
		TransportConnector mqtt = new TransportConnector();
		mqtt.setUri(new URI("mqtt://0.0.0.0:" + graph.broker().secondaryPort()));
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

	final class BrokerManager {
		private final Map<String, Producer> producers = new HashMap<>();
		private final Map<String, List<TopicConsumer>> consumers = new HashMap<>();
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
			startTanks();
			Logger.info("JMS service: started!");
		}

		void stop() {
			try {
				Logger.info("Stopping bus");
				consumers.values().forEach(c -> c.forEach(TopicConsumer::stop));
				consumers.clear();
				producers.values().forEach(Producer::close);
				producers.clear();
				session.close();
//			connection.stop();
				session = null;
				connection = null;
				service.stop();
				Logger.info("bus stopped");
			} catch (Throwable e) {
			}
		}

		private void startNessSession() {
			try {
				connection = new ActiveMQConnectionFactory("vm://" + "ness").createConnection("ness", "ness");
				connection.setClientID("ness");
				session = connection.createSession(false, AUTO_ACKNOWLEDGE);
				advisoryManager.start(session);
				connection.start();
				Logger.info("Ness session started!");
			} catch (JMSException e) {
				Logger.error(e.getMessage(), e);
			}
		}

		private void startTanks() {
			if (graph.datalake() != null) {
				File stage = new File(graph.broker().path(), "stage");
				graph.datalake().tankList().stream().filter(Datalake.Tank::isEvent).
						forEach(t -> brokerManager.registerConsumer(t.name(), new TopicSaver(stage, t.name(), Scale.valueOf(graph.datalake().scale().name())).create()));
			}
		}

		private Session nessSession() {
			if (this.session == null || closedSession()) startNessSession();
			return session;
		}

		void registerConsumer(String topic, Consumer consumer) {
			List<TopicConsumer> value = new ArrayList<>();
			if (!this.consumers.containsKey(topic)) this.consumers.put(topic, value);
			else value = this.consumers.get(topic);
			TopicConsumer topicConsumer = new TopicConsumer(nessSession(), topic);
			topicConsumer.listen(consumer);
			value.add(topicConsumer);
		}

		TopicProducer getTopicProducer(String topic) {
			try {
				if (!this.producers.containsKey(topic)) this.producers.put(topic, new TopicProducer(nessSession(), topic));
				return (TopicProducer) this.producers.get(topic);
			} catch (JMSException e) {
				Logger.error(e.getMessage(), e);
				return null;
			}
		}

		void stopConsumersOf(String topic) {
			if (!this.consumers.containsKey(topic)) return;
			this.consumers.get(topic).forEach(TopicConsumer::stop);
			this.consumers.get(topic).clear();
		}

		private boolean closedSession() {
			return ((ActiveMQSession) session).isClosed();
		}
	}

	private class PipeManager {
		private final List<Broker.Pipe> pipes;
		private BrokerManager brokerManager;

		PipeManager(BrokerManager manager, List<Broker.Pipe> pipes) {
			this.brokerManager = manager;
			this.pipes = pipes;
		}

		void start() {
			for (Broker.Pipe pipe : pipes) {
				brokerManager.registerConsumer(pipe.origin(), message -> send(pipe.destination(), textFrom(message)));
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
			final TopicProducer producer = brokerManager.getTopicProducer(destination);
			new Thread(() -> send(producer, message)).start();
		}

		private void send(TopicProducer producer, String finalToSend) {
			if (producer != null) producer.produce(MessageFactory.createMessageFor(finalToSend));
		}
	}

}
