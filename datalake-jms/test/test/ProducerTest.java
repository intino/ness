package test;

import io.intino.konos.alexandria.Inl;
import io.intino.konos.datalake.Datalake;
import io.intino.konos.datalake.Ness;
import io.intino.konos.jms.TopicProducer;
import io.intino.ness.inl.Message;
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

import static io.intino.konos.jms.MessageFactory.createMessageFor;
import static java.lang.Thread.sleep;
import static javax.jms.Session.AUTO_ACKNOWLEDGE;
import static org.apache.activemq.ActiveMQConnection.makeConnection;

public class ProducerTest {
	private static final Logger logger = LoggerFactory.getLogger(ProducerTest.class);
	private String url = "tcp://localhost:63000";
	private String user = "happysense-pre";
	private String password = "vhqpp7n6u5f2";
	private String topic = "v4.dialog";

	private Session session;
	private Connection connection;
	private TopicProducer topicProducer;
	private Random random;


	@Test
	@Ignore
	public void sendAttachment() {
		final Ness ness = new Ness(url, user, password, "");
		ness.connect();
		final Datalake.Tank tank = ness.add(topic);
		final Message message = new Message("dialog").set("name", "dialog1");
		message.set("ts", Instant.now().toString());
		message.set("value", "txt", "example".getBytes());
		tank.put(message);
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
		final String value = new Message("example.message").write("ts", Instant.now().toString()).write("value", random.nextInt()).toString();
		topicProducer.produce(createMessageFor(value));
		System.out.println("message sent to " + topic + " ->  " + value);
	}

	private void produceOldMessage() {
		final String value = new Message("example.message").write("ts", Instant.now().minus(2, ChronoUnit.HOURS).toString()).write("value", random.nextInt()).toString();
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
