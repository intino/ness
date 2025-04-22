import org.example.test.WizardTerminal;
import org.example.test.datamarts.master.MasterDatamart;
import org.example.test.messages.universe.Computer;
import systems.intino.eventsourcing.datahubterminal.ConnectorFactory;
import systems.intino.eventsourcing.datahubterminal.JmsConnector;
import systems.intino.eventsourcing.jms.ConnectionConfig;

import java.time.Instant;
import java.util.Random;

public class TerminalTest {
	private static final Random random = new Random();

	public static class Produce {
		public static void main(String[] args) throws InterruptedException {
			Connection conn = getConnection();
			conn.wizardTerminal().publish(new Computer("test", "Computer2")
					.ts(Instant.now())
					.applicationsKnown((double) random.nextInt(20))
					.architecture("x64")
					.usageCPU(random.nextDouble())
					.diskSize(random.nextLong(100000)));
			Thread.sleep(1000);
			conn.connector().stop();
			System.exit(0);
		}
	}

	public static Connection getConnection() {
		ConnectionConfig config = new ConnectionConfig("tcp://localhost:63000", "wizard", "wizard", "wizard");
		var datamartConfig = new systems.intino.eventsourcing.datahubterminal.datamart.ConnectionConfig();
		JmsConnector connector = (JmsConnector) ConnectorFactory.createConnector(config, null);
		connector.start();
		WizardTerminal wizardTerminal = new WizardTerminal(connector, datamartConfig);
		return new Connection(connector, wizardTerminal);
	}

	public static class Consume {
		public static void main(String[] args) throws InterruptedException {
			Connection connection = getConnection();
			MasterDatamart datamart = connection.wizardTerminal.datamart().init(null);
			datamart.computers().forEach(computer -> {
				computer.current()
				System.out.println(computer.id());
			});
			Thread.sleep(1000);
			connection.connector.stop();
		}
	}

	public record Connection(JmsConnector connector, WizardTerminal wizardTerminal) {
	}
}
