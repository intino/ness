package org.example.test.ssl;

import jakarta.jms.Connection;
import jakarta.jms.Session;
import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

public class SSL_ {

	@Test
	@Ignore
	public void sslTest() throws Exception {
		ActiveMQSslConnectionFactory connectionFactory = new ActiveMQSslConnectionFactory("ssl://localhost:63000");
		connectionFactory.setKeyStore(new File("../temp/datahub/client.jks").getAbsolutePath());
		connectionFactory.setTrustStore(new File("../temp/datahub/client.jts").getAbsolutePath());
		connectionFactory.setKeyStorePassword("xxxxx");
		connectionFactory.setTrustStorePassword("xxxx");
		connectionFactory.setUserName("consul");
		connectionFactory.setPassword("consul");
		Connection connection = connectionFactory.createConnection();
		connection.start();
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		session.close();

	}
}
