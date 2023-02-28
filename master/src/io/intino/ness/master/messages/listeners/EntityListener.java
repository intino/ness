package io.intino.ness.master.messages.listeners;

import io.intino.ness.master.Entity;

import java.time.Instant;

@FunctionalInterface
public interface EntityListener<T extends Entity> {

	void notify(Event<T> event);

	static <E extends Entity> EntityListener<E> onCreate(EntityListener<E> listener) {
		return event -> {
			if (event.type() == Event.Type.Create) listener.notify(event);
		};
	}

	static <E extends Entity> EntityListener<E> onUpdate(EntityListener<E> listener) {
		return event -> {
			if (event.type() == Event.Type.Update) listener.notify(event);
		};
	}

	static <E extends Entity> EntityListener<E> onEnable(EntityListener<E> listener) {
		return event -> {
			if (event.type() == Event.Type.Enable) listener.notify(event);
		};
	}

	static <E extends Entity> EntityListener<E> onDisable(EntityListener<E> listener) {
		return event -> {
			if (event.type() == Event.Type.Disable) listener.notify(event);
		};
	}

	static <E extends Entity> EntityListener<E> onNone(EntityListener<E> listener) {
		return event -> {
			if (event.type() == Event.Type.None) listener.notify(event);
		};
	}

	/**
	 * Indicates the response of an update request in master server
	 */
	interface Event<T extends Entity> {

		/**
		 * Returns the client name that sent the request
		 */
		String clientName();

		/**
		 * Indicates which type of action was performed. This can differ from the original intention of the request.
		 */
		Type type();

		/**
		 * The entity upon the operation was performed.
		 */
		String entityId();

		/**
		 * The entity simple class name
		 * */
		String entityClassName();

		/**
		 * The entity upon the operation was performed. It may be null.
		 */
		T entity();

		/**
		 * The original value of the message that was sent to the server.
		 */
		String value();

		/**
		 * The instant at which the request was processed.
		 */
		Instant ts();

		/**
		 * The request message id that triggered this event.
		 */
		String messageId();

		enum Type {
			/**
			 * A new entity was created in master
			 */
			Create,
			/**
			 * The entity was updated
			 */
			Update,
			/**
			 * The entity was enabled
			 */
			Enable,
			/**
			 * The entity was disabled
			 */
			Disable,
			/**
			 * No operation was performed
			 */
			None
		}
	}
}
