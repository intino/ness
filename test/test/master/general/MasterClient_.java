package master.general;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.LifecycleEvent;
import com.hazelcast.core.LifecycleListener;
import io.intino.ness.master.messages.Response;
import org.example.test.model.MasterTerminal;
import org.example.test.model.entities.Employee;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MasterClient_ {

	public static void main(String[] args) throws ExecutionException, InterruptedException {

		ClientConfig config = new ClientConfig();
		config.getNetworkConfig().addAddress("localhost:62555");
		config.getConnectionStrategyConfig().getConnectionRetryConfig()
				.setClusterConnectTimeoutMillis(1000);

		HazelcastInstance hz = HazelcastClient.newHazelcastClient(config);

		hz.getLifecycleService().addLifecycleListener(new LifecycleListener() {
			@Override
			public void stateChanged(LifecycleEvent event) {
				System.out.println(event);
			}
		});

		Runtime.getRuntime().addShutdownHook(new Thread(hz::shutdown));

//		MasterTerminal.Config config = new MasterTerminal.Config()
//				.clientName("the client")
//				.allowWriting(true)
//				.addresses(List.of("localhost:62555"))
//				.putProperty("hazelcast.logging.type", "none")
//				.putProperty("hazelcast.socket.connect.timeout.seconds", "1");
//
//		MasterTerminal terminal = MasterTerminal.create(config);
//		terminal.start();
//
//		Runtime.getRuntime().addShutdownHook(new Thread(terminal::stop));

//		terminal.publish(new Employee("1:employee", terminal).name("Pedri"));
//		terminal.publish(new Employee("2:employee", terminal).name("Gavi"));
//		terminal.publish(new Employee("3:employee", terminal).name("Asensio"));
//		terminal.publish(new Employee("4:employee", terminal).name("Ansu Fati"));
//		terminal.publish(new Employee("5:employee", terminal).name("Nico Williams"));
//
//		Future<Response<Employee>> future = terminal.disable("1:employee");
//
//		future.get();
//
//		System.out.println("1 => " + terminal.employee("1:employee"));
//
//		System.out.println("2 => " + terminal.disabled().employee("1:employee"));
//
//		terminal.publish(new Employee("6:employee", terminal).name("Unai Simon"));
//		terminal.publish(new Employee("7:employee", terminal).name("Morata"));
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
