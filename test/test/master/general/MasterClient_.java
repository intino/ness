package master.general;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import org.example.test.model.MasterTerminal;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static io.intino.ness.master.core.MasterLifecycleEvent.State.CLIENT_DISCONNECTED;

public class MasterClient_ {

	public static void main(String[] args) throws ExecutionException, InterruptedException {
//		initMasterTerminal();
		initHazelcastClientRaw();
	}

	private static void initHazelcastClientRaw() {
		ClientConfig config = new ClientConfig();
		config.setClusterName("cluster");
		config.setInstanceName("the client 1");
		config.getNetworkConfig().addAddress("localhost:62555");
//		ClientConnectionStrategyConfig connectionStrategyConfig = config.getConnectionStrategyConfig();
//		connectionStrategyConfig.setReconnectMode(ClientConnectionStrategyConfig.ReconnectMode.ON);
//		connectionStrategyConfig.getConnectionRetryConfig().setClusterConnectTimeoutMillis(1000);

//		config.getSecurityConfig().setUsernamePasswordIdentityConfig("client1", "client1_password");
//		config.setCredentials(new UsernamePasswordCredentials("client1", "client1_password"));

		HazelcastInstance hz = HazelcastClient.getOrCreateHazelcastClient(config);

		Runtime.getRuntime().addShutdownHook(new Thread(hz::shutdown));

		System.out.println("done");
	}

	private static void initMasterTerminal() {
		MasterTerminal.Config config = new MasterTerminal.Config()
				.clientName("the client")
				.allowWriting(true)
				.addresses(List.of("localhost:62555"))
				.putProperty("hazelcast.logging.type", "none");

		config.connectionConfig(new MasterTerminal.Config.ConnectionConfig().clusterConnectTimeoutMillis(5000));

		MasterTerminal terminal = MasterTerminal.create(config);

		terminal.start();

		terminal.addLifecycleListener(event -> {
			if(event.state() == CLIENT_DISCONNECTED) {
				// ...
			}
		});

		Runtime.getRuntime().addShutdownHook(new Thread(terminal::stop));

//		Installation entity = new Installation(UUID.randomUUID() + ":installation", terminal);
//		entity.name("Hola");
//		entity.type(Installation.Type.Videometry);
//		entity.url("");
//
//		terminal.disable("123:zone");
//		terminal.enableZone("123");
//
//		Future<Response<Entity>> future = terminal.publish(entity);
//
//		Response<Entity> response = future.get();
//
//		System.out.println("done");
	}

	public static class ConnectionConfig {

		private int initialBackoffMillis = 1000;
		private int maxBackoffMillis = 30000;
		private float multiplier = 1.05f;
		private int clusterConnectTimeoutMillis = - 1;
		private float jitter = 0;

		public int initialBackoffMillis() {
			return initialBackoffMillis;
		}

		public ConnectionConfig initialBackoffMillis(int initialBackoffMillis) {
			this.initialBackoffMillis = initialBackoffMillis;
			return this;
		}

		public int maxBackoffMillis() {
			return maxBackoffMillis;
		}

		public ConnectionConfig maxBackoffMillis(int maxBackoffMillis) {
			this.maxBackoffMillis = maxBackoffMillis;
			return this;
		}

		public float multiplier() {
			return multiplier;
		}

		public ConnectionConfig multiplier(float multiplier) {
			this.multiplier = multiplier;
			return this;
		}

		public int clusterConnectTimeoutMillis() {
			return clusterConnectTimeoutMillis;
		}

		public ConnectionConfig clusterConnectTimeoutMillis(int clusterConnectTimeoutMillis) {
			this.clusterConnectTimeoutMillis = clusterConnectTimeoutMillis;
			return this;
		}

		public float jitter() {
			return jitter;
		}

		public ConnectionConfig jitter(float jitter) {
			this.jitter = jitter;
			return this;
		}
	}
}
