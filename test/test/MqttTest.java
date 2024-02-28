import io.intino.alexandria.jms.BrokerConnector;
import org.apache.activemq.broker.SslContext;
import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.junit.Test;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

public class MqttTest {
	static File jts = new File("/Users/oroncal/workspace/infrastructure/ness/test/res/server.keystore");
	static File jks = new File("/Users/oroncal/workspace/infrastructure/ness/test/res/client.keystore");


	public static void main(String[] args) throws Exception {
		MQTT mqtt = new MQTT();
		mqtt.setConnectAttemptsMax(1);
		mqtt.setReconnectAttemptsMax(0);
		mqtt.setHost("ssl://localhost:" + "1885");
		mqtt.setClientId("test-client");
		mqtt.setCleanSession(true);
		mqtt.setUserName("user1");
		mqtt.setPassword("1234");

		ssl(mqtt);
		BlockingConnection blockingConnection = mqtt.blockingConnection();
		blockingConnection.connect();
		System.out.println("CONNECTADO!");
	}

	private static void ssl(MQTT mqtt) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, NoSuchProviderException, KeyManagementException {
		KeyStore trustStore = KeyStore.getInstance("JKS");
		trustStore.load(new FileInputStream(jts), "password".toCharArray());

		KeyStore keyStore = KeyStore.getInstance("JKS");
		keyStore.load(new FileInputStream(jks), "password".toCharArray());
		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
		keyManagerFactory.init(keyStore, "password".toCharArray());

		TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
		trustManagerFactory.init(trustStore);
		SslContext sslContext = new SslContext(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
		mqtt.setSslContext(sslContext.getSSLContext());
	}


	@Test
	public void nameTcp() throws JMSException {
//		Connection connection = BrokerConnector.createConnection(new ConnectionConfig("ssl://localhost:" + "1885", "monet", "monet", "monet-client", jks, jts, "password", "password"), null);
//		Connection connection = BrokerConnector.createConnection(new ConnectionConfig("tcp://localhost:" + "63000", "monet", "monet", "monet-client"), null);
		Connection connection = BrokerConnector.createConnection("tcp://localhost:" + "1884", "user1", "1234", null);
		connection.start();
	}
}
