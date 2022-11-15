package master;

import org.example.test.model.MasterTerminal;
import org.example.test.model.entities.Employee;

public class Client {

	public static void main(String[] args) {

		MasterTerminal terminal = MasterTerminal.create();
		terminal.start();

		terminal.publish(new Employee("123:employee", terminal));
	}
}
