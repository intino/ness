package master.examples;

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
public class CreateMasterTerminal {

	/**
	 * This will create a MasterTerminal with its default configuration
	 * This is equivalent to MasterTerminal.create(new MasterTerminal.Config())
	 * See MasterTerminal.Config class to see the default values
	 * A custom config instance is recommended
	 **/
	public MasterTerminal createDefault() {
		return MasterTerminal.create();
	}

	/**
	 * This will create a MasterTerminal with your custom configuration
	 * This is the recommended way of creating a MasterTerminal instance
	 **/
	public MasterTerminal createWithCustomConfig() {
		MasterTerminal.Config config = new MasterTerminal.Config()
				.clientName("my client") // App or module name is recommended here
				.allowWriting(true) // Allows this instance to update the master data. Default is false
				.type(MasterTerminal.Type.FullLoad) // Selects the implementation of the MasterTerminal instance
				.addresses(List.of("address to master server (<datahub-host>:<master-port>)")) // List of addresses to master server cluster members
				.multithreadLoading(true) // Load the data from master using multiple threads. Only applicable if the instance is FullLoad
				.filter(MasterTerminal.EntityFilter.OnlyEnabled) // Only show entities that passes this filter. The default is OnlyEnabled
				.cacheDisabledView(true) // Tells whether the terminal should cache the disabled view instance. Recommended when using FullLoad implementation. The default value is true
				.putProperty("some other property", "value"); // Specify other properties (specially useful for timeouts, see https://docs.hazelcast.com/hazelcast/5.1/fault-tolerance/timeouts)

		return MasterTerminal.create(config);
	}
}
