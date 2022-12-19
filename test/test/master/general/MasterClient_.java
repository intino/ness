package master.general;

import io.intino.alexandria.terminal.JmsConnector;
import io.intino.test.datahubtest.TestTerminal;
import io.intino.test.datahubtest.master.Entities;
import io.intino.test.datahubtest.master.entities.LogModule;

import java.io.File;

public class MasterClient_ {

	public static void main(String[] args) {
		TestTerminal terminal = createTerminal();
		Entities entities = terminal.entities();
		LogModule logModule = entities.logModule("123");
		entities.publish(new LogModule("123").name("hola"));
	}

	private static TestTerminal createTerminal() {
		JmsConnector connector = new JmsConnector(
				"failover:(tcp://localhost:62123)",
				"test",
				"test",
				"test",
				new File("temp/cache")
		);
		connector.start();
		return new TestTerminal(connector);
	}
}
