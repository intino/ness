package org.example.test;

import io.intino.alexandria.event.message.MessageEvent;
import io.intino.alexandria.message.Message;
import io.intino.alexandria.terminal.JmsConnector;
import io.intino.alexandria.zim.ZimStream;
import io.intino.alexandria.zim.ZimWriter;
import io.intino.test.datahubtest.TestTerminal;
import io.intino.test.datahubtest.datamarts.master.MasterDatamart;
import io.intino.test.datahubtest.messages.assertions.UserAssertion;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class Client {

	static MessageEvent event = new MessageEvent(new Message("UserAssertion")
			.set("ss", "test")
			.set("ts", Instant.now())
			.set("id", "user1")
			.set("name", "Cristian")
			.set("language", "es"));

	public static void main(String[] args) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ZimWriter zimWriter = new ZimWriter(outputStream);
		zimWriter.write(event.toMessage());
		zimWriter.close();

		byte[] bytes = outputStream.toByteArray();
		List<Message> messages = ZimStream.of(new ByteArrayInputStream(bytes)).collect(Collectors.toList());

		TestTerminal terminal = new TestTerminal(connector());
		MasterDatamart datamart = terminal.masterDatamart();

		terminal.publish(event);

		System.out.println(datamart.user("user1"));
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
