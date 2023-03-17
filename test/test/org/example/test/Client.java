package org.example.test;

import io.intino.alexandria.event.message.MessageEvent;
import io.intino.alexandria.message.Message;
import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.junit.Ignore;
import org.junit.Test;

import javax.jms.Connection;
import javax.jms.Session;
import java.io.File;
import java.time.Instant;

public class Client {

	@Test
	@Ignore
	public void sslTest() throws Exception {
		ActiveMQSslConnectionFactory connectionFactory = new ActiveMQSslConnectionFactory("ssl://localhost:63000");
		connectionFactory.setKeyStore(new File("../temp/datahub/client.jks").getAbsolutePath());
		connectionFactory.setTrustStore(new File("../temp/datahub/client.jts").getAbsolutePath());
		connectionFactory.setKeyStorePassword("vaquep0");
		connectionFactory.setTrustStorePassword("vaquep0");
		connectionFactory.setUserName("consul");
		connectionFactory.setPassword("consul");
		Connection connection = connectionFactory.createConnection();
		connection.start();
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		session.close();

	}

	static MessageEvent event = new MessageEvent(new Message("UserAssertion")
			.set("ss", "test")
			.set("ts", Instant.now())
			.set("id", "user1")
			.set("name", "Cristian")
			.set("language", "es"));


}
