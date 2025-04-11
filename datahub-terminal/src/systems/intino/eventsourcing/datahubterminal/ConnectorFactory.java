package systems.intino.eventsourcing.datahubterminal;

import systems.intino.eventsourcing.jms.ConnectionConfig;

import java.io.File;

public class ConnectorFactory {

	public static Connector createConnector(ConnectionConfig config, File outboxDirectory) {
		if (config.url().startsWith("test")) return new StubConnector(config, outboxDirectory);
		return new JmsConnector(config, outboxDirectory);
	}
}
