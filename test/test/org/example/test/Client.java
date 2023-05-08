package org.example.test;

import io.intino.alexandria.terminal.JmsConnector;
import io.intino.cosmos.datahub.TrooperTerminal;
import io.intino.cosmos.datahub.datamarts.master.MasterDatamart;

import java.io.File;
import java.util.List;

public class Client {

	public static void main(String[] args) {
		TrooperTerminal terminal = new TrooperTerminal(connector("test", "test", "test"));
		terminal.initDatamarts();

		MasterDatamart dm = terminal.masterDatamart();

		List<MasterDatamart.TimelineNode> t1 = dm.timelines("1").toList();
		List<MasterDatamart.TimelineNode> t2 = dm.timelines("2").toList();

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
