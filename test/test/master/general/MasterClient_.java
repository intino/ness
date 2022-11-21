package master.general;

import io.intino.ness.master.messages.Response;
import org.example.test.model.MasterTerminal;
import org.example.test.model.entities.Employee;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MasterClient_ {

	public static void main(String[] args) throws ExecutionException, InterruptedException {
		MasterTerminal.Config config = new MasterTerminal.Config()
				.clientName("the client")
				.allowWriting(true)
				.putProperty("hazelcast.logging.type", "none")
				.addresses(List.of("localhost:62555"));

		MasterTerminal terminal = MasterTerminal.create(config);
		terminal.start();

		Runtime.getRuntime().addShutdownHook(new Thread(terminal::stop));

		terminal.publish(new Employee("1:employee", terminal).name("Pedri"));
		terminal.publish(new Employee("2:employee", terminal).name("Gavi"));
		terminal.publish(new Employee("3:employee", terminal).name("Asensio"));
		terminal.publish(new Employee("4:employee", terminal).name("Ansu Fati"));
		terminal.publish(new Employee("5:employee", terminal).name("Nico Williams"));

		Future<Response<Employee>> future = terminal.disable("1:employee");

		future.get();

		System.out.println("1 => " + terminal.employee("1:employee"));

		System.out.println("2 => " + terminal.disabled().employee("1:employee"));

		terminal.publish(new Employee("6:employee", terminal).name("Unai Simon"));
		terminal.publish(new Employee("7:employee", terminal).name("Morata"));

		System.out.println("done");
	}
}
