package master.examples;

import io.intino.test.datahubtest.TestTerminal;
import io.intino.test.datahubtest.master.Entities;
import io.intino.test.datahubtest.master.entities.Zone;
import io.intino.test.datahubtest.master.structs.GeoPoint;

import java.util.List;
import java.util.Set;

/**
 * Examples of how to modify master data
 *
 * To use these methods, your MasterTerminal instance configuration must define allowWriting as true
 *
 * If you want to see how to subscribe to changes asynchronously, see ListenersMasterExample
 *
 * */
public class C_ModifyingMasterData {

	private TestTerminal terminal;

	/**
	 * Create or update the specified entity. If the entity was already registered, it will update its contents.
	 * If the entity does not change at all, nothing will be updated and the event type will be None
	 *
	 * The changes will be visible to all members of master.
	 *
	 * This operation is synchronous.
	 *
	 * In FullLoad implementation, the local state will be updated when the server notifies the terminal.
	 *
	 * */
	public void createOrUpdateEntity() throws Exception {
		Entities entities = terminal.entities();

		Zone zone = new Zone("my_zone_id")
				.ownerZone("other:zone")
				.children(List.of("A", "B", "C"))
				.place(List.of(new GeoPoint(0, 0), new GeoPoint(1, 1)));

		entities.publish(zone);
	}

	/**
	 * Enables or disables a specific entity. If the entity does not exist, or it was already enabled/disabled, it will do nothing, and
	 * an event of type None will be notified.
	 *
	 * As in the publish method, the operations are synchronous.
	 *
	 * */
	public void enableOrDisableEntity() {
		terminal.entities().enableEmployee("123");
		terminal.entities().enable("123:employee");

		terminal.entities().disableEmployee("123");
		terminal.entities().disable("123:employee");
	}
}
