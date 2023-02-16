package master.general;

import io.intino.alexandria.terminal.JmsConnector;
import io.intino.test.datahubtest.TestTerminal;
import io.intino.test.datahubtest.master.Entities;
import io.intino.test.datahubtest.master.entities.AnomalyType;
import io.intino.test.datahubtest.master.entities.Team;
import io.intino.test.datahubtest.master.structs.Channel;

import java.io.File;
import java.util.List;

public class MasterClient_ {

	public static void main(String[] args) {
		TestTerminal terminal = createTerminal();
		Entities entities = terminal.entities();
		Team team = new Team("team1:team");
		team.displayName("team_name");
		team.channels(List.of(new Channel("ch1", 1), new Channel("ch2", 2), new Channel("ch3", 3)));
		entities.publish(team);
		entities.publish(new AnomalyType("anomalyType1").group("Group1").issueEN("Anomaly 1").issueES("Anomalía 1").issuePT("Anomalia 1"));
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
