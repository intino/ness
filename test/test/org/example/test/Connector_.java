package org.example.test;

import io.intino.alexandria.terminal.JmsConnector;
import jakarta.jms.Message;
import org.apache.activemq.command.ActiveMQTextMessage;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class Connector_ {

	public static void main(String[] args) throws Exception {
		JmsConnector c = new JmsConnector(
				new io.intino.alexandria.jms.ConnectionConfig("failover:(tcp://localhost:63000)", "test", "test", "test"),
				new File("temp/cache")
		);

		c.start();

		Message message = c.requestResponse("service.ness.datalake.messagestore", downloadRequest(), 1, TimeUnit.MICROSECONDS);

		System.out.println(message);

		System.exit(0);
	}

	private static javax.jms.Message downloadRequest() throws Exception {
		ActiveMQTextMessage message = new ActiveMQTextMessage();
		message.setText("datamart:master:");
		return message;
	}
}
