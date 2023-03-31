package org.example.test;

import io.intino.alexandria.message.MessageReader;
import io.intino.alexandria.terminal.JmsConnector;
import io.intino.test.datahubtest.TestTerminal;
import io.intino.test.datahubtest.messages.inventory.ConsulAssertion;

import java.io.File;
import java.io.FileInputStream;

public class PublisherClient {

	public static final int DELAY = 1000;

	public static void main(String[] args) throws Exception {
		var terminal = new TestTerminal(connector());

		File target = new File("temp/consul-assertion-to-publish.inl");
		File ok = new File("temp/ok");

		target.getParentFile().mkdirs();

		target.delete();
		ok.delete();

		while(true) {
			if(!target.exists()) continue;
			try(MessageReader reader = new MessageReader(new FileInputStream(target))) {
				if(!reader.hasNext()) continue;
				ConsulAssertion event = new ConsulAssertion(reader.next());
				System.out.println(event + "\n\n");
				terminal.publish(event);
			}
			target.delete();
			ok.createNewFile();
			Thread.sleep(DELAY);
		}
	}

	private static JmsConnector connector() {
		JmsConnector jmsConnector = new JmsConnector(
				new io.intino.alexandria.jms.ConnectionConfig("failover:(tcp://localhost:63000)", "test2", "test2", "test2"),
				new File("temp/cache")
		);
		jmsConnector.start();
		return jmsConnector;
	}
}
