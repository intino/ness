package org.example.test;

import io.intino.alexandria.event.message.MessageEvent;
import io.intino.alexandria.message.Message;
import io.intino.alexandria.terminal.JmsConnector;
import io.intino.ness.master.reflection.EntityDefinition;
import io.intino.test.datahubtest.TestTerminal;
import io.intino.test.datahubtest.datamarts.master.MasterDatamart;
import io.intino.test.datahubtest.datamarts.master.entities.JavaApplication;
import io.intino.test.datahubtest.datamarts.master.entities.User;

import java.io.File;
import java.io.IOException;
import java.time.Instant;

public class Client {

	static MessageEvent event = new MessageEvent(new Message("UserAssertion")
			.set("ss", "test")
			.set("ts", Instant.now())
			.set("id", "user1")
			.set("name", "Cristian")
			.set("language", "es"));

	public static void main(String[] args) throws IOException {
		try {

			TestTerminal terminal = new TestTerminal(connector());
			MasterDatamart datamart = terminal.masterDatamart();

			System.out.println(JavaApplication.definition.descendants());

			datamart.observable("hola");

			var def = JavaApplication.definition;
			System.out.println(def.attributes());
			System.out.println(def.declaredAttributes());

//			System.out.println(datamart.user("user1").name());
//			terminal.publish(new UserAssertion(event));
//			System.out.println(datamart.user("user1").name());

		} finally {
			EntityDefinition definition = User.definition;
			System.out.println();
		}
	}

	private static JmsConnector connector() {
		JmsConnector jmsConnector = new JmsConnector(
				"failover:(tcp://localhost:63000)",
				"test", "test", "test",
				new File("temp/cache")
		);
		jmsConnector.start();
		return jmsConnector;
	}
}
