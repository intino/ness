package master.general;

import io.intino.alexandria.terminal.JmsConnector;
import io.intino.test.datahubtest.TestTerminal;
import io.intino.test.datahubtest.master.Entities;
import io.intino.test.datahubtest.master.entities.Zone;

import java.io.File;
import java.util.List;

public class MasterClient_ {

	public static void main(String[] args) {
		TestTerminal terminal = createTerminal();
		Entities entities = terminal.entities();
		List<Zone> zones = entities.zoneList();
		System.out.println(zones);
	}

	private static TestTerminal createTerminal() {
		return new TestTerminal(new JmsConnector(
				"localhost:62123",
				"test",
				"test",
				"test",
				new File("temp/cache")
		));
	}
}
