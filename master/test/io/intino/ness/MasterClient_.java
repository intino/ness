package io.intino.ness;

import io.intino.alexandria.Json;
import io.intino.ness.master.messages.Response;
import org.example.test.model.MasterTerminal;
import org.example.test.model.entities.Employee;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MasterClient_ {

	public static void main(String[] args) throws ExecutionException, InterruptedException {
		MasterTerminal.Config config = new MasterTerminal.Config()
				.instanceName("the client")
				.allowWriting(true)
				.putProperty("hazelcast.logging.type", "none")
				.addresses(List.of("localhost:62555"));

		MasterTerminal terminal = MasterTerminal.create(config);
		terminal.start();

		Runtime.getRuntime().addShutdownHook(new Thread(terminal::stop));

		Employee employee = new Employee("123:employee", terminal).name("CR7");

		Future<Response<Employee>> future = terminal.publish(employee);
		Response<Employee> response = future.get();

		System.out.println(Json.toJsonPretty(response.event()));
		System.out.println(Json.toJsonPretty(response.error()));

		Employee employee2 = response.event().entity();

		System.out.println(employee2.equals(employee));

		System.out.println("done");
	}
}
