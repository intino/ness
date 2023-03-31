package org.example.test;

import io.intino.alexandria.terminal.JmsConnector;
import io.intino.test.datahubtest.TestTerminal;
import io.intino.test.datahubtest.datamarts.master.MasterDatamart;
import io.intino.test.datahubtest.messages.inventory.ConsulAssertion;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

import static java.nio.file.StandardOpenOption.CREATE;
import static org.junit.Assert.*;

// Start Server, then PublisherClient, and then run this test
// Working directory should be $ProjectFileDir$
public class Client {

	private static final String ss = "default";

	private static TestTerminal terminal;
	private static MasterDatamart datamart;

	@BeforeClass
	public static void setup() {
		terminal = new TestTerminal(connector());
		datamart = terminal.masterDatamart();
	}

	@Test
	public void should_update_master_from_other_app() throws Exception {
		System.out.println("\nother-app");

		ConsulAssertion event;
		String id = "consul-" + System.currentTimeMillis();

		assertNull(datamart.consul(id));

		System.out.println("Creating event");

		event = new ConsulAssertion(ss, id);
		event.installedActivities(List.of("activity1"));
		notifyPublisherApp(event);

		assertNotNull(datamart.consul(id));
		assertNotNull(datamart.consul(id).installedActivities());
		assertEquals(List.of("activity1"), datamart.consul(id).installedActivities());

		System.out.println("Event modified 1");

		event = new ConsulAssertion(ss, id);
		event.installedActivities(List.of("activity1", "activity2"));
		notifyPublisherApp(event);

		assertNotNull(datamart.consul(id));
		assertNotNull(datamart.consul(id).installedActivities());
		assertEquals(List.of("activity1", "activity2"), datamart.consul(id).installedActivities());

		System.out.println("Event modified 2");

		event = new ConsulAssertion(ss, id);
		event.installedActivities(List.of("activity1", "activity2"));
		notifyPublisherApp(event);

		assertNotNull(datamart.consul(id));
		assertNotNull(datamart.consul(id).installedActivities());
		assertEquals(List.of("activity1", "activity2"), datamart.consul(id).installedActivities());

		System.out.println("Event modified 3");

		event = new ConsulAssertion(ss, id);
		event.installedActivities(Collections.emptyList());
		notifyPublisherApp(event);

		assertNotNull(datamart.consul(id));
		assertNotNull(datamart.consul(id).installedActivities());
		assertEquals(Collections.emptyList(), datamart.consul(id).installedActivities());

		System.out.println("Done: " + datamart.consul(id));
	}

	private void notifyPublisherApp(ConsulAssertion event) throws Exception {
		File ok = new File("temp/ok");
		ok.delete();
		File file = new File("temp/consul-assertion-to-publish.inl");
		Files.writeString(file.toPath(), event.toString(), CREATE);
		waitUntilOkFileIsPresent(ok);
	}

	private void waitUntilOkFileIsPresent(File ok) throws InterruptedException {
		while(true) {
			Thread.sleep(1000);
			if(ok.exists()) return;
		}
	}

	@Test
	public void should_update_master_from_same_app() {
		System.out.println("\nsame-app");

		ConsulAssertion event;
		String id = "consul-" + System.currentTimeMillis();

		assertNull(datamart.consul(id));

		System.out.println("Creating event");

		event = new ConsulAssertion(ss, id);
		event.installedActivities(List.of("activity1"));
		terminal.publish(event);

		assertNotNull(datamart.consul(id));
		assertNotNull(datamart.consul(id).installedActivities());
		assertEquals(List.of("activity1"), datamart.consul(id).installedActivities());

		System.out.println("Event modified 1");

		event = new ConsulAssertion(ss, id);
		event.installedActivities(List.of("activity1", "activity2"));
		terminal.publish(event);

		assertNotNull(datamart.consul(id));
		assertNotNull(datamart.consul(id).installedActivities());
		assertEquals(List.of("activity1", "activity2"), datamart.consul(id).installedActivities());

		System.out.println("Event modified 2");

		event = new ConsulAssertion(ss, id);
		event.installedActivities(List.of("activity1", "activity2"));
		terminal.publish(event);

		assertNotNull(datamart.consul(id));
		assertNotNull(datamart.consul(id).installedActivities());
		assertEquals(List.of("activity1", "activity2"), datamart.consul(id).installedActivities());

		System.out.println("Event modified 3");

		event = new ConsulAssertion(ss, id);
		event.installedActivities(Collections.emptyList());
		terminal.publish(event);

		assertNotNull(datamart.consul(id));
		assertNotNull(datamart.consul(id).installedActivities());
		assertEquals(Collections.emptyList(), datamart.consul(id).installedActivities());

		System.out.println("Done: " + datamart.consul(id));
	}

	private static JmsConnector connector() {
		JmsConnector jmsConnector = new JmsConnector(
				new io.intino.alexandria.jms.ConnectionConfig("failover:(tcp://localhost:63000)", "test", "test", "test"),
				new File("temp/cache")
		);
		jmsConnector.start();
		return jmsConnector;
	}
}
