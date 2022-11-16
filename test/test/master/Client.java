package master;

import org.example.test.model.MasterTerminal;
import org.example.test.model.MasterView;
import org.example.test.model.entities.Employee;
import org.example.test.model.entities.Theater;

import java.util.List;
import java.util.stream.Stream;

public class Client {

	public static void main(String[] args) {

		MasterTerminal terminal = MasterTerminal.create();
		terminal.start();

		Theater theater = terminal.disabled().theater("123:theater");
		Stream<Theater> theaters = terminal.disabled().theaters();
		List<Theater> theaterList = terminal.disabled().theaterList();

		terminal.publish(new Employee("123:employee", terminal));
	}
}
