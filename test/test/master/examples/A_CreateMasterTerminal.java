package master.examples;

import io.intino.ness.master.core.MasterInitializationException;
import org.example.test.model.MasterTerminal;

import java.util.List;

/**
 * The MasterTerminal instance should be created at the start of the application
 * Multiple instances of the terminal are allowed, though it is not recommended
 *
 * You must call MasterTerminal.start() method before using it, and call MasterTerminal.stop() when
 * the application exits or the terminal is no longer of any use. Calling it inside a shutdown hook is strongly recommended.
 *
 * When calling start, the terminal will block the caller thread until it is completely initialized.
 * */
public class A_CreateMasterTerminal {

	/**
	 * This will create a MasterTerminal with its default configuration
	 * This is equivalent to MasterTerminal.create(new MasterTerminal.Config())
	 * See MasterTerminal.Config class to see the default values
	 * A custom config instance is recommended
	 **/
	public void createDefault() {
		MasterTerminal terminal = MasterTerminal.create();

		Runtime.getRuntime().addShutdownHook(new Thread(terminal::stop));

		terminal.start();
	}

	/**
	 * This will create a MasterTerminal with your custom configuration
	 * This is the recommended way of creating a MasterTerminal instance
	 **/
	public void createWithCustomConfig() {
		MasterTerminal.Config config = new MasterTerminal.Config()
				.clientName("my client") // App or module name is recommended here
				.allowWriting(true) // Allows this instance to update the master data. Default is false
				.type(MasterTerminal.Type.FullLoad) // Selects the implementation of the MasterTerminal instance
				.addresses(List.of("address to master server (<datahub-host>:<master-port>)")) // List of addresses to master server cluster members
				.multithreadLoading(true) // Load the data from master using multiple threads. Only applicable if the instance is FullLoad
				.filter(MasterTerminal.EntityFilter.OnlyEnabled) // Only show entities that pass this filter. The default is OnlyEnabled
				.cacheDisabledView(true) // Tells whether the terminal should cache the disabled view instance. Recommended when using FullLoad implementation. The default value is true
				.putProperty("some other property", "value"); // Specify other properties (especially useful for timeouts, see https://docs.hazelcast.com/hazelcast/5.1/fault-tolerance/timeouts)

		// Connection retry config. If not set, it will try to connect infinitely.
		MasterTerminal.Config.ConnectionConfig connectionConfig = new MasterTerminal.Config.ConnectionConfig()
				.clusterConnectTimeoutMillis(60 * 1000)
				.initialBackoffMillis(10000)
				.maxBackoffMillis(60 * 1000)
				.multiplier(1.05f)
				.jitter(0);

		config.connectionConfig(connectionConfig);

		MasterTerminal terminal = MasterTerminal.create(config);
		terminal.start();
	}

	/**
	 * start() will block until the terminal can connect to a master server. If it cannot connect to any server, and if
	 * clusterConnectTimeoutMillis was set to a value > 0, it will throw a MasterInitializationException after the specified timeout.
	 * */
	public void handleErrorsOnStart() {
		MasterTerminal terminal = MasterTerminal.create();
		try {
			terminal.start();
		} catch (MasterInitializationException e) {
			// TODO
		}
	}

	/**
	 * You can listen to changes to the MasterTerminal backend. This is specially useful if you want to be notified when
	 * the terminal loses connection or reconnects to a server.
	 *
	 * The listener must be added AFTER calling start.
	 * */
	public void handleLifecycleEvents() {
		MasterTerminal terminal = MasterTerminal.create();
		terminal.start();

		terminal.addLifecycleListener(event -> {
			switch(event.state()) {
				case STARTING:
					break;
				case STARTED:
					break;
				case SHUTTING_DOWN:
					break;
				case SHUTDOWN:
					break;
				case MERGING:
					break;
				case MERGED:
					break;
				case MERGE_FAILED:
					break;
				case CLIENT_CONNECTED:
					break;
				case CLIENT_DISCONNECTED:
					break;
				case CLIENT_CHANGED_CLUSTER:
					break;
			}
		});
	}
}
