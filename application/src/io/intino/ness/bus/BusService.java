package io.intino.ness.bus;

import io.intino.ness.graph.JMSConnector;
import io.intino.ness.graph.Tank;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.broker.BrokerService;
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
import org.apache.activemq.network.jms.JmsConnector;
import org.apache.activemq.network.jms.OutboundTopicBridge;
import org.apache.activemq.network.jms.SimpleJmsTopicConnector;
import org.apache.activemq.plugin.java.JavaRuntimeConfigurationBroker;
import org.apache.activemq.plugin.java.JavaRuntimeConfigurationPlugin;
import org.apache.activemq.security.AuthenticationUser;
import org.apache.activemq.security.SimpleAuthenticationPlugin;
import org.apache.activemq.store.kahadb.KahaDBPersistenceAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.security.Principal;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.intino.ness.graph.JMSConnector.Direction.incoming;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class BusService {
	private static final Logger logger = LoggerFactory.getLogger(BusService.class);
	private static final String NESS = "ness";

	private final BrokerService service;
	private final int brokerPort;
	private final int mqttPort;
	private final JavaRuntimeConfigurationPlugin configurationPlugin;

	private final SimpleAuthenticationPlugin authenticator;
	private Map<String, VirtualDestinationInterceptor> pipes = new HashMap<>();
	private File brokerStore;
	private JavaRuntimeConfigurationBroker confBroker;

	public BusService(int brokerPort, int mqttPort, boolean persistent, File brokerStore, Map<String, String> users, List<Tank> tanks, List<JMSConnector> connectors) {
		this.brokerPort = brokerPort;
		this.mqttPort = mqttPort;
		this.brokerStore = getCanonicalFile(brokerStore);
		this.service = new BrokerService();
		this.authenticator = new SimpleAuthenticationPlugin(initUsers(users));
		this.configurationPlugin = new JavaRuntimeConfigurationPlugin();
		configure(persistent, tanks, connectors);
	}

	public Map<String, List<String>> users() {
		Map<String, List<String>> users = new LinkedHashMap<>();
		Map<String, Set<Principal>> userGroups = authenticator.getUserGroups();
		for (String user : userGroups.keySet())
			if (!user.equals(NESS))
				users.put(user, userGroups.get(user).stream().map(Principal::getName).collect(toList()));
		return users;
	}

	public String newUser(String name) {
		if (authenticator.getUserPasswords().containsKey(name)) return null;
		String password = PasswordGenerator.nextPassword();
		authenticator.getUserPasswords().put(name, password);
		authenticator.getUserGroups().put(name, Collections.emptySet());
		return password;
	}

	public void removeUser(String name) {
		if (name.equals(NESS)) return;
		authenticator.getUserGroups().remove(name);
		authenticator.getUserPasswords().remove(name);
	}

	public void addJMSConnector(JMSConnector c) {
		try {
			SimpleJmsTopicConnector connector = new SimpleJmsTopicConnector();
			connector.setName(c.name$());
			connector.setLocalTopicConnectionFactory(new ActiveMQConnectionFactory("octavioroncal", "octavioroncal", "tcp://localhost:63000"));
			connector.setOutboundTopicConnectionFactory(new ActiveMQConnectionFactory(c.bus().user(), c.bus().password(), c.bus().originURL()));
			connector.setOutboundUsername(c.bus().user());
			connector.setOutboundPassword(c.bus().password());
			if (c.direction().equals(incoming)) connector.setInboundTopicBridges(toInboundBridges(c.topics()));
			else connector.setOutboundTopicBridges(toOutboundBridges(c.topics()));
			service.addJmsConnector(connector);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}


	public List<JmsConnector> jmsConnectors() {
		return Arrays.asList(service.getJmsBridgeConnectors());
	}

	public List<String> topics() {
		try {
			return Arrays.stream(service.getBroker().getDestinations())
					.map(ActiveMQDestination::getPhysicalName).filter(n -> !n.contains("ActiveMQ.Advisory")).collect(Collectors.toList());
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	public void start() {
		try {
			service.start();
			service.waitUntilStarted();
			confBroker = (JavaRuntimeConfigurationBroker) service.getBroker().getAdaptor(JavaRuntimeConfigurationBroker.class);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void stop() {
		try {
			service.stop();
			service.waitUntilStopped();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
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
			logger.error(e.getMessage(), e);
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
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	void removeTopic(ActiveMQDestination destination) {
		try {
			if (destination != null) service.getBroker().removeDestination(service.getAdminConnectionContext(), destination, 0);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	Broker broker() {
		try {
			return service.getBroker();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	private OutboundTopicBridge[] toOutboundBridges(List<String> outboundTopics) {
		return outboundTopics.stream().map(topicName -> {
			OutboundTopicBridge bridge = new OutboundTopicBridge(topicName);
			bridge.setLocalTopicName(topicName);
			return bridge;
		}).toArray(OutboundTopicBridge[]::new);
	}

	private InboundTopicBridge[] toInboundBridges(List<String> inboundTopics) {
		return inboundTopics.stream().map(topicName -> {
			InboundTopicBridge bridge = new InboundTopicBridge(topicName);
			bridge.setLocalTopicName(topicName);
			return bridge;
		}).toArray(InboundTopicBridge[]::new);
	}

	private List<AuthenticationUser> initUsers(Map<String, String> modelUsers) {
		ArrayList<AuthenticationUser> users = new ArrayList<>();
		users.add(new AuthenticationUser(NESS, NESS, "admin"));
		users.add(new AuthenticationUser("octavioroncal", "octavioroncal", "admin"));
		for (Map.Entry<String, String> entry : modelUsers.entrySet())
			users.add(new AuthenticationUser(entry.getKey(), entry.getValue(), ""));
		return users;
	}

	private void configure(boolean persistence, List<Tank> tanks, List<JMSConnector> connectors) {
		try {
			service.setBrokerName(NESS);
			if (persistence) {
				service.setPersistent(false);
				service.setPersistenceAdapter(persistenceAdapter());
			} else {
				service.setPersistent(false);
				service.setPersistenceAdapter(null);
			}
			service.setRestartAllowed(true);
			service.setUseJmx(true);
			service.setUseShutdownHook(true);
			service.setAdvisorySupport(true);
			service.setPlugins(new BrokerPlugin[]{authenticator, configurationPlugin});
			addPolicies();
			addTCPConnector();
			addMQTTConnector();
			initTanks(tanks);
			initConnectors(connectors);
		} catch (Exception e) {
			logger.error("Error configuring: " + e.getMessage(), e);
		}
	}

	private void initTanks(List<Tank> tanks) {
		tanks.stream().filter(Tank::running).forEach(tank -> pipe(tank.feedQN(), tank.flowQN()));
		updateInterceptors();
	}

	private void initConnectors(List<JMSConnector> connectors) {
		for (JMSConnector c : connectors) addJMSConnector(c);
	}

	public void updateInterceptors() {
		try {
			final VirtualDestinationInterceptor[] interceptors = pipes.values().toArray(new VirtualDestinationInterceptor[0]);
			service.setDestinationInterceptors(interceptors);
			if (confBroker == null) return;
			List<VirtualDestination> destinations = new ArrayList<>();
			Arrays.stream(interceptors).forEach(i -> Collections.addAll(destinations, i.getVirtualDestinations()));
			confBroker.setVirtualDestinations(destinations.toArray(new VirtualDestination[0]), true);
			TimeUnit.SECONDS.sleep(2);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
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
		connector.setUri(new URI("tcp://0.0.0.0:" + brokerPort + "?transport.useInactivityMonitor=false"));
		connector.setName("OWireConn");
		service.addConnector(connector);
	}

	private void addMQTTConnector() throws Exception {
		TransportConnector mqtt = new TransportConnector();
		mqtt.setUri(new URI("mqtt://0.0.0.0:" + mqttPort));
		mqtt.setName("MQTTConn");
		service.addConnector(mqtt);
	}

	private KahaDBPersistenceAdapter persistenceAdapter() {
		KahaDBPersistenceAdapter adapter = new KahaDBPersistenceAdapter();
		adapter.setDirectoryArchive(new File(brokerStore, "archive"));
		adapter.setIndexDirectory(new File(brokerStore, "index"));
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

	static final class PasswordGenerator {
		static String nextPassword() {
			return new BigInteger(130, new SecureRandom()).toString(32).substring(0, 12);
		}
	}
}
