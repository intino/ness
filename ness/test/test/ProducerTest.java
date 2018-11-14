package test;

import io.intino.alexandria.inl.Message;
import io.intino.alexandria.jms.TopicProducer;
import io.intino.alexandria.nessaccessor.NessAccessor;
import io.intino.alexandria.nessaccessor.tcp.TCPEventStore;
import io.intino.alexandria.nessaccessor.tcp.TcpDatalake;
import io.intino.ness.Inl;
import io.intino.ness.core.Datalake;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.Session;
import java.io.File;
import java.nio.file.Files;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static io.intino.alexandria.jms.MessageFactory.createMessageFor;
import static java.lang.Thread.sleep;
import static javax.jms.Session.AUTO_ACKNOWLEDGE;
import static org.apache.activemq.ActiveMQConnection.makeConnection;

public class ProducerTest {
	private static final Logger logger = LoggerFactory.getLogger(ProducerTest.class);
	private String url = "tcp://localhost:63000";
	private String user = "test";
	private String password = "test";
	private String topic = "consul.serverstatus";

	private Session session;
	private Connection connection;
	private TopicProducer topicProducer;
	private Random random;


	@Test
	@Ignore
	public void sendAttachment() {
		NessAccessor ness = new NessAccessor(new TcpDatalake(url, user, password, ""));
		ness.connection().connect();
		Datalake.EventStore.Tank tank = ness.eventStore().tank(topic);
		final Message message = new Message("status").set("name", "dialog1");
		message.set("ts", Instant.now().toString());
		message.attach("value", "txt", "example".getBytes());
		((TCPEventStore) ness.eventStore()).feed(tank.name(), message);
	}

	@Test
	@Ignore
	public void produceDialogs() {
		try {
			final List<Message> messages = Inl.load(new String(Files.readAllBytes(new File("/Users/oroncal/workspace/ness/application/test/dialogs.inl").toPath())));
			messages.sort(Comparator.comparing(m -> Instant.parse(m.get("instant"))));
			messages.forEach(this::produceMessage);
		} catch (Exception ignored) {
		}
	}

	@Test
	@Ignore
	public void produceSurveys() {
		try {
			final List<Message> messages = Inl.load(new String(Files.readAllBytes(new File("/Users/oroncal/workspace/ness/application/test/surveys.inl").toPath())));
			messages.sort(Comparator.comparing(m -> Instant.parse(m.get("ts"))));
			messages.forEach(this::produceMessage);
		} catch (Exception ignored) {
		}
	}

	@Test
	@Ignore
	public void produceOld() {
		try {
			while (true) {
				produceOldMessage();
				sleep(1000);
			}
		} catch (Exception ignored) {
		}
	}

	private void produceMessage(Message message) {
		topicProducer.produce(createMessageFor(message.toString()));
//		System.out.println("message sent to " + topic + " -> " + message.get("ts"));
	}

	private void produceMessage() {
		final String value = new Message("example.message").set("ts", Instant.now().toString()).set("value", random.nextInt()).toString();
		topicProducer.produce(createMessageFor(value));
		System.out.println("message sent to " + topic + " ->  " + value);
	}

	private void produceOldMessage() {
		final String value = new Message("example.message").set("ts", Instant.now().minus(2, ChronoUnit.HOURS).toString()).set("value", random.nextInt()).toString();
		topicProducer.produce(createMessageFor(value));
		System.out.println("message sent to " + topic + " ->  " + value);
	}

	@Before
	public void setUp() throws Exception {
		initSession();
		random = new Random(2123132);
	}

	private void initSession() {
		try {
			connection = makeConnection(user, password, url);
			connection.start();
			this.session = connection.createSession(false, AUTO_ACKNOWLEDGE);
			topicProducer = new TopicProducer(session, topic);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}
