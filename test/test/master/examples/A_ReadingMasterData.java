package master.examples;

import io.intino.test.datahubtest.TestTerminal;
import io.intino.test.datahubtest.master.Entities;
import io.intino.test.datahubtest.master.EntitiesView;
import io.intino.test.datahubtest.master.entities.Employee;
import io.intino.test.datahubtest.master.entities.Theater;

import java.util.List;
import java.util.stream.Stream;

public class A_ReadingMasterData {

	private TestTerminal terminal;

	/**
	 * Entities will expose 3 methods for each type of entity.
	 * The EntityFilter specified in the configuration will be applied here.
	 *
	 * Notes:
	 *
	 * Lists are immutable. If you modify the list itself, it won't affect the internal state of MasterTerminal nor the server data
	 *
	 * The entities returned are meant to be read-only. This means that, if you change an entity by calling a setter, you will only be
	 * changing the object in local memory. The original object in the master server will not be modified.
	 *
	 * You can only get the entities that your terminal subscribed to. If you try to access an entity not specified in the Subscribe
	 * section in the terminal declaration, an UnsupportedOperationException will be thrown.
	 *
	 * To modify the entities, see the ModifyingMasterData examples
	 *
	 * */
	public void getEntities() {
		Entities entities = terminal.entities();

		Employee employee = entities.employee("123:employee");
		Stream<Employee> employees = entities.employees();
		List<Employee> employeeList = entities.employeeList();
	}

	/**
	 * You can access disabled entities (entity.enabled() == false) if you call the Entities.disabled() method
	 *
	 * This only has an effect if the EntityFilter specified on MasterTerminal creation was NOT OnlyDisabled.
	 * If the filter already was OnlyDisabled, it will return itself.
	 *
	 * Implementation is dependent on the original MasterTerminal implementation. Please note that if config.cacheDisabledView was set
	 * to false, it will create a new instance every time MasterTerminal.disabled() is called. This is especially critical if the implementation
	 * is FullLoad, because it will download all the required entities from master to local memory on creation.
	 *
	 * */
	public void getDisabledEntities() {
		EntitiesView disabledView = terminal.entities().disabled();

		Theater theater = disabledView.theater("123:theater");
		Stream<Theater> theaters = disabledView.theaters();
		List<Theater> theaterList = disabledView.theaterList();

		// theater.enabled() will always be false
	}
}
