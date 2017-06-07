package io.intino.ness.bus;

import io.intino.konos.jms.Consumer;
import io.intino.konos.jms.TopicConsumer;
import io.intino.konos.jms.TopicProducer;
import io.intino.ness.box.NessBox;
import io.intino.ness.graph.User;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.broker.region.DestinationInterceptor;
import org.apache.activemq.broker.region.virtual.CompositeTopic;
import org.apache.activemq.broker.region.virtual.VirtualDestination;
import org.apache.activemq.broker.region.virtual.VirtualDestinationInterceptor;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.jaas.GroupPrincipal;
import org.apache.activemq.security.AuthenticationUser;
import org.apache.activemq.security.SimpleAuthenticationPlugin;
import org.apache.activemq.store.kahadb.KahaDBPersistenceAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Session;
import java.io.File;
import java.math.BigInteger;
import java.net.URI;
import java.security.Principal;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static javax.jms.Session.AUTO_ACKNOWLEDGE;

public final class BusManager {
	private Logger logger = LoggerFactory.getLogger(BusManager.class);
	private static final String NESS = "ness";
	private final NessBox box;
	private final BrokerService service;
	private final SimpleAuthenticationPlugin authenticator;
	private final Map<String, TopicProducer> producers = new HashMap<>();
	private final Map<String, TopicConsumer> consumers = new HashMap<>();
	private Session session;

	public BusManager(NessBox box) {
		this.box = box;
		service = new BrokerService();
		authenticator = new SimpleAuthenticationPlugin(initUsers());
		configure();
	}

	public void start() {
		try {
			service.start();
			session = nessSession();
			startAdvisories();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public Map<String, List<String>> users() {
		Map<String, List<String>> users = new LinkedHashMap<>();
		Map<String, Set<Principal>> userGroups = authenticator.getUserGroups();
		for (String user : userGroups.keySet())
			if (!user.equals(NESS))
				users.put(user, userGroups.get(user).stream().map(Principal::getName).collect(toList()));
		return users;
	}

	public String addUser(String name, List<String> groups) {
		if (authenticator.getUserPasswords().containsKey(name) || groups.contains("admin")) return null;
		String password = PasswordGenerator.nextPassword();
		authenticator.getUserPasswords().put(name, password);
		authenticator.getUserGroups().put(name, groups.stream().map(GroupPrincipal::new).collect(Collectors.toSet()));
		box.ness().create("users", name).user(password, groups).save$();
		return password;
	}

	public boolean removeUser(String name) {
		if (name.equals(NESS)) return false;
		authenticator.getUserGroups().remove(name);
		authenticator.getUserPasswords().remove(name);
		User user1 = box.ness().userList(user -> user.name$().equals(name)).findFirst().orElse(null);
		user1.core$().delete();
		return true;
	}

	public boolean cleanTank(String topic) {
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

	public void pipe(String from, String to) {
		ActiveMQDestination fromTopic = findTopic(from);
		ActiveMQDestination toTopic = findTopic(to);
		CompositeTopic virtualTopic = new CompositeTopic();
		try {
			virtualTopic.setName(from);
			virtualTopic.intercept(service.getDestination(fromTopic));
			virtualTopic.setForwardTo(Collections.singletonList(service.getDestination(toTopic)));
			VirtualDestinationInterceptor interceptor = new VirtualDestinationInterceptor();
			interceptor.setVirtualDestinations(new VirtualDestination[]{virtualTopic});
			service.setDestinationInterceptors(new DestinationInterceptor[]{interceptor});
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}


	public void registerConsumer(String feedQN, Consumer consumer) {
		consumers.putIfAbsent(feedQN, new TopicConsumer(session, feedQN));
		TopicConsumer topicConsumer = consumers.get(feedQN);
		topicConsumer.listen(consumer, NESS + "-" + feedQN);
	}

	public TopicProducer registerOrGetProducer(String path) {
		try {
			producers.putIfAbsent(path, new TopicProducer(session, path));
			return producers.get(path);
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	public TopicConsumer consumerOf(String feedQN) {
		return consumers.get(feedQN);
	}

	public void quit() {
		try {
			consumers.values().forEach(TopicConsumer::stop);
			session.close();
			service.stop();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void startAdvisories() throws JMSException {
//		Destination advisoryDestination = AdvisorySupport.getDestinationAdvisoryTopic(AdvisorySupport.QUEUE_ADVISORY_TOPIC);
//		session.createConsumer(advisoryDestination).setMessageListener(new TopicListener(box, session));
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

	private Session nessSession() {
		try {
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://ness");
			javax.jms.Connection connection = connectionFactory.createConnection(NESS, NESS);
			connection.setClientID(NESS);
			connection.start();
			return connection.createSession(false, AUTO_ACKNOWLEDGE);
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
			return null;
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
			service.setPersistenceAdapter(persistenceAdapter());
			service.setUseJmx(true);
			service.setUseShutdownHook(true);
			service.setAdvisorySupport(true);
			service.setPlugins(new BrokerPlugin[]{authenticator});
			TransportConnector connector = new TransportConnector();
			connector.setUri(new URI("tcp://0.0.0.0:" + box.brokerPort()));
			service.addConnector(connector);
			TransportConnector mqtt = new TransportConnector();
			mqtt.setUri(new URI("mqtt://0.0.0.0:" + box.mqttPort()));
			mqtt.setName("MQTTConn");
			service.addConnector(mqtt);
		} catch (Exception e) {
			logger.error("Error configuring: " + e.getMessage(), e);
		}
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

	public List<String> topics() {
		try {
			ActiveMQDestination[] destinations = service.getBroker().getDestinations();
			return Arrays.stream(destinations).map(ActiveMQDestination::getPhysicalName).filter(n -> !n.contains("ActiveMQ.Advisory")).collect(Collectors.toList());
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	static final class PasswordGenerator {
		static String nextPassword() {
			return new BigInteger(130, new SecureRandom()).toString(32).substring(0, 12);
		}
	}
}
