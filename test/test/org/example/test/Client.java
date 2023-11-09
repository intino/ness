package org.example.test;

import io.intino.alexandria.terminal.JmsConnector;
import io.intino.cosmos.datahub.TrooperTerminal;
import io.intino.cosmos.datahub.datamarts.master.MasterDatamart;
import io.intino.cosmos.datahub.datamarts.master.MasterDatamartImpl;
import io.intino.cosmos.datahub.datamarts.master.entities.Observable;
import io.intino.cosmos.datahub.datamarts.master.entities.Observer;
import io.intino.cosmos.datahub.datamarts.master.entities.Place;
import io.intino.ness.master.Datamart;
import io.intino.ness.master.model.Concept;
import io.intino.sumus.chronos.Timeline;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class Client {

	public static void main(String[] args) throws IOException, MasterDatamart.TimelineNotAvailableException {

		TrooperTerminal terminal = new TrooperTerminal(connector("test", "test", "test"));
		terminal.initDatamarts();

		MasterDatamart.TimelineNode.AlwaysDownloadFromDatahub.set(true);

		MasterDatamartImpl dm = (MasterDatamartImpl) terminal.datamart();

		Observer observer = dm.observer("tcconsul-472DSTUNELIN");

		System.out.println(observer.installedActivities());
		System.out.println(observer.container());

		observer.addChangeListener((concept, attribute, oldValue) -> {
			System.out.println("update -> " + attribute);
		});

		System.out.println("--> waiting for change...");

		System.out.println(observer.installedActivities());
		System.out.println(observer.container());
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
