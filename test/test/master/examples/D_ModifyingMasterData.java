//package master.examples;
//
//import io.intino.ness.master.messages.MasterMessageException;
//import io.intino.ness.master.messages.Response;
//import io.intino.ness.master.messages.listeners.EntityListener;
//import io.intino.ness.master.messages.listeners.ErrorListener;
//import io.intino.ness.master.model.Entity;
//import org.example.test.model.MasterTerminal;
//import org.example.test.model.entities.Employee;
//
//import java.time.LocalDateTime;
//import java.util.concurrent.Future;
//
///**
// * Examples of how to modify master data
// *
// * To use these methods, your MasterTerminal instance configuration must define allowWriting as true
// *
// * If you want to see how to subscribe to changes asynchronously, see ListenersMasterExample
// *
// * */
//public class D_ModifyingMasterData {
//
//	private MasterTerminal terminal;
//
//	/**
//	 * Create or update the specified entity. If the entity was already registered, it will update its contents.
//	 * If the entity does not change at all, nothing will be updated and the event type will be None
//	 *
//	 * The changes will be visible to all members of master.
//	 *
//	 * This operation is asynchronous. This means that the changes will not be visible until the server has processed the request.
//	 * Use the returned Future object to block until it has been processed by the server.
//	 *
//	 * In FullLoad implementation, the local state will be updated when the server notifies the terminal.
//	 *
//	 * */
//	public void createOrUpdateEntity() throws Exception {
//		Employee employee = new Employee("123:employee", terminal)
//				.name("the name")
//				.area("123:area")
//				.email("user123@email.com")
//				.datetime(LocalDateTime.now());
//
//		// Asynchronous
//		terminal.publish(employee);
//
//		// Synchronous
//		Future<Response<Employee>> future = terminal.publish(employee);
//		Response<Employee> response = future.get();
//
//		if(response.success()) {
//			// The entity was successfully created/updated
//			EntityListener.Event<Employee> event = response.event(); // Only non-null when response.success() is true
//			Entity.Id entityId = event.entityId(); // Always non-null
//			Employee entity = event.entity(); // May be null, depending on the original request
//			// ...
//		} else {
//			// An error occurred in the server while processing this request
//			ErrorListener.Error error = response.error(); // Only non-null when response.success() is false
//			MasterMessageException exception = error.cause();
//			// ...
//		}
//	}
//
//	/**
//	 * Enables or disables a specific entity. If the entity does not exist, or it was already enabled/disabled, it will do nothing, and
//	 * an event of type None will be notified.
//	 *
//	 * The changes in client will depend on the EntityFilter indicated on the MasterTerminal creation.
//	 *
//	 * As in the publish method, the operations are asynchronous, and you can wait for them using the returned future.
//	 *
//	 * */
//	public void enableOrDisableEntity() {
//
//		terminal.enable("123:employee");
//
//		terminal.disable("123:employee");
//
//		Future<Response<Entity>> future = terminal.disable("123:employee");
//		// ...
//	}
//}
