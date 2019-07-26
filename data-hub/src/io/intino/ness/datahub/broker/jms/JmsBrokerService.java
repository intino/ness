package io.intino.ness.datahub.broker.jms;

import io.intino.alexandria.logger.Logger;
import io.intino.ness.datahub.broker.BrokerService;
import io.intino.ness.datahub.graph.Broker;
import io.intino.ness.datahub.graph.NessGraph;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.broker.region.policy.ConstantPendingMessageLimitStrategy;
import org.apache.activemq.broker.region.policy.PolicyEntry;
import org.apache.activemq.broker.region.policy.PolicyMap;
import org.apache.activemq.broker.region.virtual.CompositeTopic;
import org.apache.activemq.broker.region.virtual.VirtualDestination;
import org.apache.activemq.broker.region.virtual.VirtualDestinationInterceptor;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.activemq.network.jms.InboundTopicBridge;
import org.apache.activemq.network.jms.SimpleJmsTopicConnector;
import org.apache.activemq.plugin.java.JavaRuntimeConfigurationBroker;
import org.apache.activemq.plugin.java.JavaRuntimeConfigurationPlugin;
import org.apache.activemq.security.AuthenticationUser;
import org.apache.activemq.security.SimpleAuthenticationPlugin;
import org.apache.activemq.store.kahadb.KahaDBPersistenceAdapter;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;

import static java.util.Collections.singletonList;

public class JmsBrokerService implements BrokerService {
	private static final String NESS = "ness";
	private final File brokerStore;
	private final NessGraph graph;

	private org.apache.activemq.broker.BrokerService service;
	private Map<String, VirtualDestinationInterceptor> pipes = new HashMap<>();
	private JavaRuntimeConfigurationBroker confBroker;

	public JmsBrokerService(File brokerStore, NessGraph graph) {
		this.brokerStore = getCanonicalFile(brokerStore);
		this.graph = graph;
	}

	public void start() {
		try {
			Logger.info("starting broker...");
			configure();
			this.service.start();
			this.service.waitUntilStarted();
			this.confBroker = (JavaRuntimeConfigurationBroker) service.getBroker().getAdaptor(JavaRuntimeConfigurationBroker.class);
			Logger.info("broker started!");
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}
	}

	private void configure() {
		try {
			SimpleAuthenticationPlugin authenticator = new SimpleAuthenticationPlugin(registerUsers());
			JavaRuntimeConfigurationPlugin configurationPlugin = new JavaRuntimeConfigurationPlugin();
			service = new org.apache.activemq.broker.BrokerService();
			service.setBrokerName(NESS);
			service.setPersistent(true);
			service.setPersistenceAdapter(persistenceAdapter());
			service.setDataDirectory(new File(brokerStore, "activemq-data").getAbsolutePath());
			service.setRestartAllowed(true);
			service.setUseJmx(true);
			service.setUseShutdownHook(true);
			service.setAdvisorySupport(true);
			service.setPlugins(new BrokerPlugin[]{authenticator, configurationPlugin});
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


	public void stop() {
		try {
			if (service != null) {
				service.stop();
				service.waitUntilStopped();
			}
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}
	}


	public void pipe(String from, String to) {
		try {
			CompositeTopic compositeTopic = new CompositeTopic();
			compositeTopic.setName(from);
			compositeTopic.setForwardOnly(false);
			compositeTopic.setForwardTo(singletonList(new ActiveMQTopic(to)));
			VirtualDestinationInterceptor newInterceptor = new VirtualDestinationInterceptor();
			newInterceptor.setVirtualDestinations(new VirtualDestination[]{compositeTopic});
			pipes.put(from + "#" + to, newInterceptor);
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}
	}

	void stopPipe(String fromTopic, String toTopic) {
		if (!pipes.containsKey(fromTopic + "#" + toTopic)) return;
		pipes.remove(fromTopic + "#" + toTopic);
		updateInterceptors();
	}

	public Map<String, VirtualDestinationInterceptor> pipes() {
		return pipes;
	}

	ActiveMQDestination findTopic(String topic) {
		try {
			ActiveMQDestination[] destinations = service.getBroker().getDestinations();
			if (destinations == null) return null;
			return Arrays.stream(destinations).filter(d -> d.getPhysicalName().equals(topic)).findFirst().orElse(null);
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
			return null;
		}
	}

	void removeTopic(ActiveMQDestination destination) {
		try {
			if (destination != null) service.getBroker().removeDestination(service.getAdminConnectionContext(), destination, 0);
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}
	}

	org.apache.activemq.broker.Broker broker() {
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

	private void updateInterceptors() {
		try {
			final VirtualDestinationInterceptor[] interceptors = pipes.values().toArray(new VirtualDestinationInterceptor[0]);
			service.setDestinationInterceptors(interceptors);
			if (confBroker == null) return;
			List<VirtualDestination> destinations = new ArrayList<>();
			Arrays.stream(interceptors).forEach(i -> Collections.addAll(destinations, i.getVirtualDestinations()));
			confBroker.setVirtualDestinations(destinations.toArray(new VirtualDestination[0]), false);
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
		adapter.setDirectoryArchive(new File(brokerStore, "archive"));
		adapter.setIndexDirectory(new File(brokerStore, "entries"));
		adapter.setDirectory(brokerStore);
		adapter.setBrokerName(NESS);
		adapter.setBrokerService(service);
		return adapter;
	}

	private File getCanonicalFile(File brokerStore) {
		try {
			return brokerStore.getCanonicalFile();
		} catch (IOException e) {
			return brokerStore;
		}
	}
}
