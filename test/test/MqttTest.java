import io.intino.alexandria.jms.BrokerConnector;
import io.intino.alexandria.jms.ConnectionConfig;
import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import org.apache.activemq.broker.SslContext;
import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.junit.Test;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

public class MqttTest {

	@Test
	public void name() throws Exception {
		MQTT mqtt = new MQTT();
		mqtt.setConnectAttemptsMax(1);
		mqtt.setReconnectAttemptsMax(0);
		mqtt.setHost("ssl://localhost:" + "1885");
		mqtt.setClientId("test-client");
		mqtt.setCleanSession(true);
		mqtt.setUserName("monet");
		mqtt.setPassword("monet");

		ssl(mqtt);
		BlockingConnection blockingConnection = mqtt.blockingConnection();
		blockingConnection.connect();
		System.out.println();
	}

	private static void ssl(MQTT mqtt) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, NoSuchProviderException, KeyManagementException {
		File jts = new File("/Users/oroncal/workspace/infrastructure/ness/temp/datahub/certs/terminal.jts");
		File jks = new File("/Users/oroncal/workspace/infrastructure/ness/temp/datahub/certs/terminal.jks");


		KeyStore trustStore = KeyStore.getInstance("JKS");
		trustStore.load(new FileInputStream(jts), "oAhGPgdAHQ3yDvohTFDc".toCharArray());

		KeyStore keyStore = KeyStore.getInstance("JKS");
		keyStore.load(new FileInputStream(jks), "gdgJW6gnjz_Kr.pvQtVq".toCharArray());
		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
		keyManagerFactory.init(keyStore, "gdgJW6gnjz_Kr.pvQtVq".toCharArray());

		TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
		trustManagerFactory.init(trustStore);
		SslContext sslContext = new SslContext(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
		mqtt.setSslContext(sslContext.getSSLContext());
	}


	@Test
	public void nameTcp() throws JMSException {
		File jts = new File("/Users/oroncal/workspace/infrastructure/ness/temp/datahub/certs/terminal.jts");
		File jks = new File("/Users/oroncal/workspace/infrastructure/ness/temp/datahub/certs/terminal.jks");
		Connection connection = BrokerConnector.createConnection(new ConnectionConfig("ssl://localhost:" + "1885", "monet", "monet", "monet-client", jks, jts, "gdgJW6gnjz_Kr.pvQtVq", "oAhGPgdAHQ3yDvohTFDc"), null);
//		Connection connection = BrokerConnector.createConnection(new ConnectionConfig("tcp://localhost:" + "63000", "monet", "monet", "monet-client"), null);
//		Connection connection = BrokerConnector.createConnection(new ConnectionConfig("tcp://localhost:" + "1884", "monet", "monet", "monet-client"), null);
		connection.start();
	}
}
