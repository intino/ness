package master;

import io.intino.ness.master.messages.MasterMessageException;
import io.intino.ness.master.messages.Response;
import io.intino.ness.master.messages.listeners.EntityListener;
import io.intino.ness.master.messages.listeners.ErrorListener;
import org.example.test.model.MasterTerminal;
import org.example.test.model.entities.Employee;
import org.example.test.model.entities.Theater;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Stream;

public class Client {

	public static void main(String[] args) throws ExecutionException, InterruptedException {

		MasterTerminal terminal = MasterTerminal.create();
		terminal.start();

		Employee e = new Employee("", terminal);

		Theater theater = terminal.disabled().theater("123:theater");
		Stream<Theater> theaters = terminal.disabled().theaters();
		List<Theater> theaterList = terminal.disabled().theaterList();

		Future<Response<Employee>> future = terminal.publish(new Employee("123:employee", terminal));

		Response<Employee> response = future.get();

		if(response.success()) {
			EntityListener.Event<Employee> event = response.event();
			Employee entity = event.entity();
			// ...
		} else {
			ErrorListener.Error error = response.error();
			MasterMessageException cause = error.cause();
			// ...
		}
	}
}
