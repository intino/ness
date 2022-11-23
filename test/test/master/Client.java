package master;

import org.example.test.model.MasterTerminal;

import java.util.concurrent.ExecutionException;

public class Client {

	public static void main(String[] args) throws ExecutionException, InterruptedException {

		MasterTerminal.Config config = new MasterTerminal.Config();
		config.putProperty("hazelcast.socket.connect.timeout.seconds", "1");

		MasterTerminal terminal = MasterTerminal.create(config);
		terminal.start();


	}
}
