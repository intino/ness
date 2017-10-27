package io.intino.ness.box;

import com.google.gson.Gson;
import io.intino.konos.jms.TopicProducer;
import io.intino.tara.io.Node;
import io.intino.tara.io.Stash;
import io.intino.tara.io.StashDeserializer;
import io.intino.tara.io.StashSerializer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Session;
import java.io.File;
import java.nio.file.Files;
import java.time.Instant;

import static io.intino.konos.jms.MessageFactory.createMessageFor;
import static io.intino.ness.box.Feeder.nessSession;
import static java.lang.Thread.sleep;

public class Service {

	private static final Logger logger = LoggerFactory.getLogger(Service.class);

	public static void main(String[] args) throws JMSException, InterruptedException {
		Session session = nessSession();
		start(new TopicProducer(session, "service.consul.73dcbc934bfe8541"));
		start(new TopicProducer(session, "service.consul.7510b6841e7d59ba"));
	}

	private static void start(TopicProducer producer) {
		new Thread(() -> {
			while (true)
				try {
					producer.produce(createMessageFor(jsonMessage()));
					System.out.println("sent!");
					sleep(1000);
				} catch (InterruptedException ignored) {
				}
		}).start();
	}

	private static String jsonMessage() {
		return new Gson().toJson(new ExampleMessage(java.util.UUID.randomUUID().toString(), Math.random() * 60));
	}

	public static class ExampleMessage {
		Instant created = Instant.now();
		String user;
		double value;

		public ExampleMessage(String user, double value) {
			this.user = user;
			this.value = value;
		}
	}

	@Test
	public void name() throws Exception {
		Stash stash = StashDeserializer.stashFrom(new File("/Users/oroncal/workspace/ness/application/test/busPipes.stash"));
		for (Node node : stash.nodes) {
			node.facets.remove("Aqueduct");
			node.facets.add("BusPipe");
		}
		Files.write(new File("newStash").toPath(), StashSerializer.serialize(stash));
	}
}
