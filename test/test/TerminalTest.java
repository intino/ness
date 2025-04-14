import org.example.test.WizardTerminal;
import org.example.test.messages.universe.Computer;
import systems.intino.eventsourcing.datahubterminal.ConnectorFactory;
import systems.intino.eventsourcing.datahubterminal.JmsConnector;
import systems.intino.eventsourcing.jms.ConnectionConfig;

import java.time.Instant;
import java.util.Random;

public class TerminalTest {

	private static Random random;

	public static void main(String[] args) throws InterruptedException {
		ConnectionConfig config = new ConnectionConfig("tcp://localhost:63000", "wizard", "wizard", "wizard");
		var datamartConfig = new systems.intino.eventsourcing.datahubterminal.datamart.ConnectionConfig();
		JmsConnector connector = (JmsConnector) ConnectorFactory.createConnector(config, null);
		connector.start();
		WizardTerminal wizardTerminal = new WizardTerminal(connector, datamartConfig);
		random = new Random();
		wizardTerminal.publish(new Computer("test", "Computer2")
				.ts(Instant.now())
				.applicationsKnown((double) random.nextInt(20))
				.architecture("x64")
				.usageCPU(random.nextDouble())
				.diskSize(random.nextLong(100000)));
		Thread.sleep(1000);
		connector.stop();
		System.exit(0);
	}
}
