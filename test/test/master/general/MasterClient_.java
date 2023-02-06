package master.general;

import io.intino.alexandria.terminal.JmsConnector;
import io.intino.test.datahubtest.TestTerminal;
import io.intino.test.datahubtest.master.Entities;

import java.io.File;

public class MasterClient_ {

	public static void main(String[] args) {
		TestTerminal terminal = createTerminal();
		Entities entities = terminal.entities();
		System.out.println();
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
