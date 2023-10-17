package org.example.test;

import io.intino.alexandria.terminal.JmsConnector;
import io.intino.cosmos.datahub.TrooperTerminal;
import io.intino.cosmos.datahub.datamarts.master.MasterDatamart;
import io.intino.cosmos.datahub.datamarts.master.MasterDatamartImpl;
import io.intino.ness.master.Datamart;
import io.intino.sumus.chronos.Timeline;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class Client {

	public static void main(String[] args) throws IOException {

		TrooperTerminal terminal = new TrooperTerminal(connector("test", "test", "test"));
		terminal.initDatamarts();

		MasterDatamart.TimelineNode.AlwaysDownloadFromDatahub.set(true);

		MasterDatamartImpl dm = (MasterDatamartImpl) terminal.datamart();

		Datamart.Dictionary.Word word = dm.dictionary().get("Brasil");

		Optional<String> valueInEnglish = dm.dictionary("tc").get("Brasil").in("en");

		Optional<String> label = dm.area("Mexico").label("en");


		var timelines = dm.timelines("45-79-45-227-ip-linodeusercontent-com").toList();

		for(var t : timelines) {
			Timeline timeline = t.get();
			System.out.println(timeline);
		}

//		Stream<MasterDatamart.TimelineNode> timelines = dm.timelines("EC2AMAZ-D67CFU1_Code");

//		MasterDatamart.TimelineNode timeline = timelines.findFirst().get();

//		dm.mount(new ApplicationJavaAssertion(new MessageReader(message()).next()));

		System.out.println();
	}

	private static String message() {
		return """
				[ApplicationJavaAssertion]
				ts: 2023-05-26T07:00:53.384336780Z
				ss: test
				id: 45-79-45-227-ip-linodeusercontent-com|outsourcing-1.4.10
				player: cr7
				a123: 456
				    
				[ApplicationJavaAssertion.Operation]
				name: status
				activity: monitor
				    
				[ApplicationJavaAssertion.Operation]
				name: start-sampling
				activity: monitor
				
				[ApplicationJavaAssertion.Operation.Procedure]
				name: theName12
				returnType: theRT435
				
				    
				[ApplicationJavaAssertion.Operation]
				name: stop-sampling
				activity: monitor
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
