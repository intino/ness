import org.example.test.WizardTerminal;
import org.example.test.messages.universe.Computer;
import org.junit.Test;
import systems.intino.eventsourcing.datahubterminal.ConnectorFactory;
import systems.intino.eventsourcing.jms.ConnectionConfig;

import java.time.Instant;

public class TerminalTest {


	@Test
	public void should_send_subject_info() {
		ConnectionConfig config = new ConnectionConfig("tcp://localhost:63000", "wizard", "wizard", "wizard");
		var datamartConfig = new systems.intino.eventsourcing.datahubterminal.datamart.ConnectionConfig();
		WizardTerminal wizardTerminal = new WizardTerminal(ConnectorFactory.createConnector(config, null), datamartConfig);
		wizardTerminal.publish(new Computer("test", "Computer1").ts(Instant.now()).applicationsKnown(10.).architecture("x64").diskSize(100000L));
	}
}
