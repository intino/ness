package org.example.test;

import io.intino.alexandria.terminal.JmsConnector;
import io.intino.cosmos.datahub.TrooperTerminal;
import io.intino.cosmos.datahub.datamarts.master.MasterDatamart;

import java.io.File;
import java.util.stream.Stream;

public class Client {

	public static void main(String[] args) {
		TrooperTerminal terminal = new TrooperTerminal(connector("test", "test", "test"));
		terminal.initDatamarts();

		MasterDatamart dm = terminal.masterDatamart();

		Stream<MasterDatamart.TimelineNode> timelines = dm.timelines("EC2AMAZ-D67CFU1_Code");

		MasterDatamart.TimelineNode timeline = timelines.findFirst().get();

		System.out.println();
	}

	private static JmsConnector connector(String user, String password, String clientId) {
		JmsConnector jmsConnector = new JmsConnector(
				new io.intino.alexandria.jms.ConnectionConfig("failover:(tcp://localhost:63000)", user, password, clientId),
				new File("temp/cache")
		);
		jmsConnector.start();
		return jmsConnector;
	}
}
