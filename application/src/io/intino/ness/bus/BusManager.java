package io.intino.ness.bus;

import io.intino.konos.jms.Consumer;
import io.intino.konos.jms.TopicConsumer;
import io.intino.ness.Ness;
import io.intino.ness.Topic;
import io.intino.ness.User;
import io.intino.ness.konos.NessBox;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.advisory.AdvisorySupport;
import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.jaas.GroupPrincipal;
import org.apache.activemq.security.AuthenticationUser;
import org.apache.activemq.security.SimpleAuthenticationPlugin;
import org.apache.activemq.store.kahadb.KahaDBPersistenceAdapter;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import java.io.File;
import java.math.BigInteger;
import java.net.URI;
import java.security.Principal;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.logging.Logger.getGlobal;
import static java.util.stream.Collectors.toList;
import static javax.jms.Session.AUTO_ACKNOWLEDGE;

public final class BusManager {

	static final String NESS = "ness";
	private final NessBox box;
	private final BrokerService service;
	private final SimpleAuthenticationPlugin authenticator;
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
			getGlobal().severe(e.getMessage());
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
		ness(box).create("users", name).user(password, groups).save();
		return password;
	}

	public boolean removeUser(String name) {
		if (name.equals(NESS)) return false;
		authenticator.getUserGroups().remove(name);
		authenticator.getUserPasswords().remove(name);
		ness(box).userList(user -> user.name().equals(name)).get(0).delete();
		return true;
	}

	public boolean cleanTopic(String topic) {
		try {
			ActiveMQDestination destination = findTopic(topic);
			if (destination == null) return false;
			broker().removeDestination(service.getAdminConnectionContext(), destination, 0);
			return true;
		} catch (Exception e) {
			getGlobal().severe(e.getMessage());
			return false;
		}
	}

	public boolean renameTopic(String topic, String newName) {
		ActiveMQDestination destination = findTopic(topic);
		if (destination == null) return false;
		destination.setPhysicalName(newName);
		return true;
	}

	public void subscribe(String topic, Consumer consumer) {
		new TopicConsumer(this.session, topic).listen(consumer);
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
			connector.setUri(new URI("tcp://localhost:" + box.get("broker.port")));
			service.addConnector(connector);
		} catch (Exception e) {
			getGlobal().severe("Error configuring: " + e.getMessage());
		}
	}

	private KahaDBPersistenceAdapter persistenceAdapter() {
		KahaDBPersistenceAdapter adapter = new KahaDBPersistenceAdapter();
		adapter.setDirectory(new File(box.get("broker.store")));
		adapter.setBrokerName(NESS);
		adapter.setBrokerService(service);
		return adapter;
	}

	private List<AuthenticationUser> initUsers() {
		ArrayList<AuthenticationUser> users = new ArrayList<>();
		users.add(new AuthenticationUser(NESS, NESS, "admin"));
		for (User user : ness(box).userList())
			users.add(new AuthenticationUser(user.name(), user.password(), String.join(",", user.groups())));
		return users;
	}

	private void startAdvisories() throws JMSException {
		Destination advisoryDestination = AdvisorySupport.getDestinationAdvisoryTopic(AdvisorySupport.QUEUE_ADVISORY_TOPIC);
		session.createConsumer(advisoryDestination).setMessageListener(new TopicListener(box, session));
	}

	private static Ness ness(NessBox box) {
		return box.graph().wrapper(Ness.class);
	}

	private ActiveMQDestination findTopic(String topic) {
		try {
			ActiveMQDestination[] destinations = broker().getDestinations();
			if (destinations == null) return null;
			return Arrays.stream(destinations).filter(d -> d.getPhysicalName().equals(topic)).findFirst().orElse(null);
		} catch (Exception e) {
			getGlobal().severe(e.getMessage());
			return null;
		}
	}

	private List<Topic> topics() {
		return ness(box).topicList();
	}

	private Session nessSession() {
		try {
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://ness");
			javax.jms.Connection connection = connectionFactory.createConnection(NESS, NESS);
			connection.start();
			return connection.createSession(false, AUTO_ACKNOWLEDGE);
		} catch (JMSException e) {
			getGlobal().severe(e.getMessage());
			return null;
		}
	}

	private Broker broker() {
		try {
			return service.getBroker();
		} catch (Exception e) {
			getGlobal().severe(e.getMessage());
			return null;
		}
	}

	static final class PasswordGenerator {

		static String nextPassword() {
			return new BigInteger(130, new SecureRandom()).toString(32).substring(0, 12);
		}
	}


}
