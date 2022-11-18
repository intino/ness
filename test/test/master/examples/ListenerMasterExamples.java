package master.examples;

import io.intino.ness.master.messages.MasterMessageException;
import io.intino.ness.master.messages.listeners.EntityListener;
import org.example.test.model.MasterTerminal;
import org.example.test.model.entities.Employee;

import java.time.Instant;

/**
 * Examples of asynchronous listeners. The server will notify all clients when a request is processed or an error occurred.
 *
 * These listeners will be called even if your terminal didn't send the original request. While this is convenient in many cases,
 * such as when updating the application's state, you may want to only listen to responses of YOUR requests. For those cases, use the Future
 * object returned from the MasterTerminal.publish, MasterTerminal.enable or MasterTerminal.disable methods.
 * See ModifyingMasterExamples for details.
 *
 * */
public class ListenerMasterExamples {

	private MasterTerminal terminal;

	/**
	 * You can add an entity listener for each entity defined in the model.
	 * Multiple listeners can be added for a single entity type, and will be notified in order.
	 * The listener will be called if the request was successful.
	 * If you want to listen to errors, use the MasterTerminal.addErrorListener method
	 *
	 * This example is using listeners for specific event types. The listener will only be called when the event.type() is the expected one.
	 * */
	public void entityListenersPerEventType() {
		terminal.addEmployeeEntityListener(EntityListener.onCreate(event -> {})); // event.type() == Create
		terminal.addEmployeeEntityListener(EntityListener.onUpdate(event -> {})); // event.type() == Update
		terminal.addEmployeeEntityListener(EntityListener.onEnable(event -> {})); // event.type() == Enable
		terminal.addEmployeeEntityListener(EntityListener.onDisable(event -> {})); // event.type() == Disable
		terminal.addEmployeeEntityListener(EntityListener.onRemove(event -> {})); // event.type() == Remove
		terminal.addEmployeeEntityListener(EntityListener.onNone(event -> {})); // event.type() == None
	}

	/**
	 * You can specify multiple listeners to subscribe when an error occur on server side when processing an update request.
	 *
	 * */
	public void errorListeners() {
		terminal.addErrorListener(error -> {

			String requestId = error.messageId();
			Instant ts = error.ts();
			MasterMessageException cause = error.cause();

			// if(!terminal.config().instanceName().equals(author)) return;
			// ...
		});
	}

	/**
	 * You can add an entity listener for each entity defined in the model.
	 * Multiple listeners can be added for a single entity type, and will be notified in order.
	 * The listener will be called if the request was successful.
	 * If you want to listen to errors, use the MasterTerminal.addErrorListener method
	 *
	 * This is the verbose and not so elegant way of doing this. If you want to listen to specific event types, see
	 * the examples in entityListenersPerEventType method.
	 * */
	public void entityListenersGeneric() {

		terminal.addEmployeeEntityListener(event -> {

			Instant ts = event.ts();
			Employee employee = event.entity();

			switch(event.type()) {
				case Create:
					// ...
					break;
				case Update:
					// ...
					break;
				case Enable:
					// ...
					break;
				case Disable:
					// ...
					break;
				case Remove:
					// ...
					break;
				case None:
					// ...
					break;
			}
		});
	}
}
