
package io.intino.ness.bus;

import io.intino.konos.jms.Consumer;
import io.intino.konos.jms.TopicConsumer;
import io.intino.konos.jms.TopicProducer;
import io.intino.ness.box.NessBox;
import io.intino.ness.graph.User;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.broker.region.DestinationInterceptor;
import org.apache.activemq.broker.region.policy.ConstantPendingMessageLimitStrategy;
import org.apache.activemq.broker.region.policy.PolicyEntry;
import org.apache.activemq.broker.region.policy.PolicyMap;
import org.apache.activemq.broker.region.virtual.CompositeTopic;
import org.apache.activemq.broker.region.virtual.VirtualDestination;
import org.apache.activemq.broker.region.virtual.VirtualDestinationInterceptor;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.security.AuthenticationUser;
import org.apache.activemq.security.SimpleAuthenticationPlugin;
import org.apache.activemq.store.kahadb.KahaDBPersistenceAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.security.Principal;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static javax.jms.Session.AUTO_ACKNOWLEDGE;
import static javax.jms.Session.SESSION_TRANSACTED;

public final class BusManager {
	private static final Logger logger = LoggerFactory.getLogger(BusManager.class);
	private static final String NESS = "ness";
	private final NessBox box;
	private final BrokerService service;
	private final SimpleAuthenticationPlugin authenticator;
	private final Map<String, TopicProducer> producers = new HashMap<>();
	private final Map<String, List<TopicConsumer>> consumers = new HashMap<>();
	private Connection connection;
	private Session session;
	private AdvisoryManager advisoryManager;

	public BusManager(NessBox box) {
		this.box = box;
		service = new BrokerService();
		authenticator = new SimpleAuthenticationPlugin(initUsers());
		configure();
	}

	public void start() {
		try {
			service.start();
			initNessSession();
			logger.info("JMS service: started!");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public Session nessSession() {
		if (this.session == null || ((ActiveMQSession) session).isClosed()) initNessSession();
		return session;
	}

	public Map<String, List<String>> users() {
		Map<String, List<String>> users = new LinkedHashMap<>();
		Map<String, Set<Principal>> userGroups = authenticator.getUserGroups();
		for (String user : userGroups.keySet())
			if (!user.equals(NESS))
				users.put(user, userGroups.get(user).stream().map(Principal::getName).collect(toList()));
		return users;
	}

	public String addUser(String name) {
		if (authenticator.getUserPasswords().containsKey(name)) return null;
		String password = PasswordGenerator.nextPassword();
		authenticator.getUserPasswords().put(name, password);
		authenticator.getUserGroups().put(name, Collections.emptySet());
		box.ness().create("users", name).user(password, Collections.emptyList()).save$();
		return password;
	}

	public boolean removeUser(String name) {
		if (name.equals(NESS)) return false;
		authenticator.getUserGroups().remove(name);
		authenticator.getUserPasswords().remove(name);
		User user = box.ness().userList(u -> u.name$().equals(name)).findFirst().orElse(null);
		user.core$().delete();
		return true;
	}

	public boolean removeTopic(String topic) {
		try {
			ActiveMQDestination destination = findTopic(topic);
			if (destination == null) return false;
			broker().removeDestination(service.getAdminConnectionContext(), destination, 0);
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}
	}

	public boolean renameTopic(String topic, String newName) {
		ActiveMQDestination destination = findTopic(topic);
		if (destination == null) return false;
		destination.setPhysicalName(newName);
		return true;
	}

	public boolean pipe(String from, String to) {
		ActiveMQDestination fromTopic = findTopic(from);
		ActiveMQDestination toTopic = findTopic(to);
		if (toTopic == null) toTopic = createTopic(to);
		if (toTopic == null) return false;
		CompositeTopic virtualTopic = new CompositeTopic();
		try {
			virtualTopic.setName(from);
			virtualTopic.intercept(service.getDestination(fromTopic));
			virtualTopic.setForwardTo(Collections.singletonList(toTopic));
			VirtualDestinationInterceptor interceptor = new VirtualDestinationInterceptor();
			interceptor.setVirtualDestinations(new VirtualDestination[]{virtualTopic});
			service.setDestinationInterceptors(new DestinationInterceptor[]{interceptor});
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}
	}

	public ActiveMQDestination createTopic(String name) {
		try {
			return (ActiveMQDestination) session.createTopic(name);
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	public void registerConsumer(String feedQN, Consumer consumer) {
		List<TopicConsumer> consumers = this.consumers.putIfAbsent(feedQN, new ArrayList<>());
		if (consumers == null) consumers = this.consumers.get(feedQN);
		TopicConsumer topicConsumer = new TopicConsumer(session, feedQN);
		topicConsumer.listen(consumer, NESS + consumers.size() + "-" + feedQN);
		consumers.add(topicConsumer);
	}

	public TopicProducer registerOrGetProducer(String path) {
		try {
			if (!producers.containsKey(path)) producers.put(path, new TopicProducer(session, path));
			return producers.get(path);
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	public List<TopicConsumer> consumersOf(String feedQN) {
		return consumers.get(feedQN);
	}

	public void quit() {
		try {
			consumers.values().forEach(c -> c.forEach(TopicConsumer::stop));
			session.close();
			service.stop();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void startAdvisories() throws JMSException {
		advisoryManager = new AdvisoryManager(broker(), session);
		advisoryManager.start();
	}

	private ActiveMQDestination findTopic(String topic) {
		try {
			ActiveMQDestination[] destinations = broker().getDestinations();
			if (destinations == null) return null;
			return Arrays.stream(destinations).filter(d -> d.getPhysicalName().equals(topic)).findFirst().orElse(null);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	private void initNessSession() {
		try {
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://ness");
			connection = connectionFactory.createConnection(NESS, NESS);
			connection.setClientID(NESS);
			session = connection.createSession(false, AUTO_ACKNOWLEDGE);
			startAdvisories();
			connection.start();
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private Broker broker() {
		try {
			return service.getBroker();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	private void configure() {
		try {
			service.setBrokerName(NESS);
//			startPersistence();
			service.setPersistent(false);
			service.setPersistenceAdapter(null);
			service.setUseJmx(true);
			service.setUseShutdownHook(true);
			service.setAdvisorySupport(true);
			service.setPlugins(new BrokerPlugin[]{authenticator});
			addPolicies();
			addTCPConnector();
			addMQTTConnector();
		} catch (Exception e) {
			logger.error("Error configuring: " + e.getMessage(), e);
		}
	}

	public void stopPersistence() {
		try {
			service.setPersistent(false);
			service.setPersistenceAdapter(null);
			service.requestRestart();
		} catch (IOException e) {
			logger.error("Error stopping persistence: " + e.getMessage(), e);
		}
	}

	public void startPersistence() {
		try {
			service.setPersistent(true);
			service.setPersistenceAdapter(persistenceAdapter());
			service.requestRestart();
		} catch (IOException e) {
			logger.error("Error starting persistence: " + e.getMessage(), e);
		}
	}

	private void addPolicies() {
		final List<PolicyEntry> policyEntries = new ArrayList<>();
		final PolicyEntry entry = new PolicyEntry();
		entry.setAdvisoryForDiscardingMessages(true);
		entry.setTopicPrefetch(1);
		ConstantPendingMessageLimitStrategy pendingMessageLimitStrategy = new ConstantPendingMessageLimitStrategy();
		pendingMessageLimitStrategy.setLimit(100000);
		entry.setPendingMessageLimitStrategy(pendingMessageLimitStrategy);
		final PolicyMap policyMap = new PolicyMap();
		policyMap.setPolicyEntries(policyEntries);
		service.setDestinationPolicy(policyMap);
	}

	private void addTCPConnector() throws Exception {
		TransportConnector connector = new TransportConnector();
		connector.setUri(new URI("tcp://0.0.0.0:" + box.brokerPort() + "?transport.useInactivityMonitor=false"));
		connector.setName("OWireConn");
		service.addConnector(connector);
	}

	private void addMQTTConnector() throws Exception {
		TransportConnector mqtt = new TransportConnector();
		mqtt.setUri(new URI("mqtt://0.0.0.0:" + box.mqttPort()));
		mqtt.setName("MQTTConn");
		service.addConnector(mqtt);
	}

	private KahaDBPersistenceAdapter persistenceAdapter() {
		KahaDBPersistenceAdapter adapter = new KahaDBPersistenceAdapter();
		adapter.setDirectory(new File(box.brokerStore()));
		adapter.setBrokerName(NESS);
		adapter.setBrokerService(service);
		return adapter;
	}

	private List<AuthenticationUser> initUsers() {
		ArrayList<AuthenticationUser> users = new ArrayList<>();
		users.add(new AuthenticationUser(NESS, NESS, "admin"));
		users.add(new AuthenticationUser("octavioroncal", "octavioroncal", "admin"));
		for (User user : box.ness().userList())
			users.add(new AuthenticationUser(user.name$(), user.password(), String.join(",", user.groups())));
		return users;
	}

	public List<String> topicsInfo() {
		try {
			List<ActiveMQDestination> destinations = Arrays.stream(service.getBroker().getDestinations()).filter(d -> !d.getPhysicalName().contains("ActiveMQ.Advisory")).collect(Collectors.toList());
			return destinations.stream().map((d) ->
					d.getPhysicalName() + " Consumers:" + advisoryManager.consumersOf(d) + " Producers:" + advisoryManager.producersOf(d) + " Enqueued:" + advisoryManager.enqueuedMessageOf(d) +
							" Enqueued:" + advisoryManager.dequeuedMessageOf(d)).collect(Collectors.toList());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return Collections.emptyList();
		}
	}

	public List<String> topics() {
		try {
			return Arrays.stream(service.getBroker().getDestinations())
					.map(ActiveMQDestination::getPhysicalName).filter(n -> !n.contains("ActiveMQ.Advisory")).collect(Collectors.toList());
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	public Session transactedSession() {
		try {
			return connection.createSession(true, SESSION_TRANSACTED);
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	static final class PasswordGenerator {
		static String nextPassword() {
			return new BigInteger(130, new SecureRandom()).toString(32).substring(0, 12);
		}
	}
}
