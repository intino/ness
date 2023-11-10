package org.example.test;

import io.intino.alexandria.message.MessageReader;
import io.intino.alexandria.terminal.JmsConnector;
import io.intino.alexandria.zim.ZimStream;
import io.intino.cosmos.datahub.TrooperTerminal;
import io.intino.cosmos.datahub.datamarts.master.MasterDatamart;
import io.intino.cosmos.datahub.datamarts.master.MasterDatamartImpl;
import io.intino.cosmos.datahub.messages.universe.ObserverAssertion;
import io.intino.ness.master.Datamart;
import io.intino.sumus.chronos.Timeline;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.stream.Collectors;

public class PublisherClient {

	public static void main(String[] args) throws IOException, MasterDatamart.TimelineNotAvailableException {

		TrooperTerminal terminal = new TrooperTerminal(connector("test2", "test2", "test2"));

		terminal.publish(new ObserverAssertion(new MessageReader(message().replace("::ts::", Instant.now().toString())).next()));

		System.out.println("Published!");
	}

	private static String message() {
		return """
				[ObserverAssertion]
				ts: ::ts::
				ss: tcconsul-472DSTUNELIN
				id: tcconsul-472DSTUNELIN
				enabled: true
				""";
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
