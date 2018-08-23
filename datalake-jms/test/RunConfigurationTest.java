import io.intino.konos.alexandria.Inl;
import io.intino.konos.datalake.Datalake;
import io.intino.konos.datalake.Ness;
import io.intino.konos.jms.TopicProducer;
import io.intino.ness.box.NessBox;
import io.intino.ness.box.NessConfiguration;
import io.intino.ness.datalake.graph.DatalakeGraph;
import io.intino.ness.graph.NessGraph;
import io.intino.tara.io.Stash;
import io.intino.tara.magritte.Graph;
import io.intino.tara.magritte.stores.InMemoryFileStore;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Ignore;
import org.junit.Test;

import javax.jms.JMSException;
import javax.jms.Session;
import java.io.File;

import static io.intino.konos.jms.MessageFactory.createMessageFor;
import static javax.jms.Session.AUTO_ACKNOWLEDGE;

public class RunConfigurationTest {


	public static void main(String[] args) {
		NessConfiguration boxConfiguration = new NessConfiguration(new String[]{"workspace=./temp/ness", "connector_id=test", "broker_port=63000", "scale=Day", "mqtt_port=1884"});
		final NessBox box = new NessBox(boxConfiguration);
		Graph graph = new Graph().loadStashes("ConfigurationTest");
		final DatalakeGraph datalakeGraph = new Graph(store(box.storeDirectory())).loadStashes("Datalake").as(DatalakeGraph.class);
		box.put(graph.as(NessGraph.class)).put(datalakeGraph);
		graph.as(NessGraph.class).tankList().forEach(t -> datalakeGraph.add(t.qualifiedName()));
		datalakeGraph.core$().save("tanks");
		box.open();
	}

	@Test@Ignore
	public void testModel() throws JMSException, InterruptedException {
		final Session session = nessSession();
		final Ness ness = new Ness("tcp://localhost:63000", "test", "test", "test_id");
		ness.connect();
		final Datalake.Tank tank = ness.add("consul.processstatus");
		tank.batchSession(100);
		final TopicProducer producer = new TopicProducer(session, "feed.consul.processstatus");
		for (int i = 0; i < 1000; i++) producer.produce(createMessageFor(message()));
		Thread.sleep(100000);
	}

	private static io.intino.tara.magritte.Store store(String directory) {
		return new InMemoryFileStore(new File(directory)) {
			@Override
			public void writeStash(Stash stash, String path) {
				stash.language = "Ness";
				super.writeStash(stash, path);
			}
		};
	}

	private static String message() {
		return Inl.toMessage(new Feeder.ExampleMessage(java.util.UUID.randomUUID().toString(), Math.random() * 60)).toString();
	}

	private static Session nessSession() {
		try {
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:63000");
			javax.jms.Connection connection = connectionFactory.createConnection("test", "test");
			connection.start();
			return connection.createSession(false, AUTO_ACKNOWLEDGE);
		} catch (JMSException e) {
			return null;
		}
	}
}
