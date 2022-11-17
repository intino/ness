package master.examples;

import org.example.test.model.MasterTerminal;
import org.example.test.model.MasterView;
import org.example.test.model.entities.Employee;
import org.example.test.model.entities.Theater;

import java.util.List;
import java.util.stream.Stream;

public class ReadingMasterData {

	private MasterTerminal terminal;

	/**
	 * MasterTerminal will expose 3 methods for each type of entity.
	 * The EntityFilter specified in the configuration will be applied here.
	 *
	 * Notes:
	 *
	 * Lists are immutable. If you modify the list itself, it won't affect the internal state of MasterTerminal not the server data
	 *
	 * The entities returned are meant to be read-only. This means that, if you change an entity by calling a setter, you will only be
	 * changing the object in local memory. The original object in the master server will not be modified.
	 *
	 * To modify the entities, see the ModifyingMasterData examples
	 *
	 * */
	public void getEntities() {
		Employee employee = terminal.employee("123:employee"); // Get by id. Returns null if not found
		Stream<Employee> employees = terminal.employees(); // Get stream of entities. Empty stream if any entity was found
		List<Employee> employeeList = terminal.employeeList(); // Get list of entities. Empty list if any entity was found

		// If you want to use the terminal as a read-only accessor to master, cast it to a MasterView interface
		MasterView masterView = terminal;

		masterView.employee("123:employee");
		masterView.employees();
		masterView.employeeList();
	}

	/**
	 * You can access disabled entities (entity.enabled() == false) if you call the MasterTerminal.disabled() method
	 *
	 * This only has an effect if the EntityFilter specified on MasterTerminal creation was NOT OnlyDisabled.
	 * If the filter already was OnlyDisabled, it will return itself.
	 *
	 * Implementation is dependent on the original MasterTerminal implementation. Please note that if config.cacheDisabledView was set
	 * to false, it will create a new instance every time MasterTerminal.disabled() is called. This is specially critical if the implementation
	 * is FullLoad, because it will download all the required entities from master to local memory on creation.
	 *
	 * */
	public void getDisabledEntities() {
		MasterView disabledView = terminal.disabled();

		Theater theater = disabledView.theater("123:theater");
		Stream<Theater> theaters = disabledView.theaters();
		List<Theater> theaterList = disabledView.theaterList();

		// theater.enabled() will always be false
	}
}
