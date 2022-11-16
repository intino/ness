package io.intino.ness.master.data;

import io.intino.ness.master.model.Entity;

import java.time.Instant;

public interface EntityListener<T extends Entity> {

	void notify(Event<T> event);

	static <E extends Entity> EntityListener<E> onCreate(EntityListener<E> listener) {
		return event -> { if(event.type() == Event.Type.Create) listener.notify(event); };
	}

	static <E extends Entity> EntityListener<E> onUpdate(EntityListener<E> listener) {
		return event -> { if(event.type() == Event.Type.Update) listener.notify(event); };
	}

	static <E extends Entity> EntityListener<E> onEnable(EntityListener<E> listener) {
		return event -> { if(event.type() == Event.Type.Enable) listener.notify(event); };
	}

	static <E extends Entity> EntityListener<E> onDisable(EntityListener<E> listener) {
		return event -> { if(event.type() == Event.Type.Disable) listener.notify(event); };
	}

	static <E extends Entity> EntityListener<E> onRemove(EntityListener<E> listener) {
		return event -> { if(event.type() == Event.Type.Remove) listener.notify(event); };
	}

	interface Event<T extends Entity> {

		String author();
		Type type();
		T entity();
		Instant ts();
		String messageId();

		enum Type {
			Create, Update, Enable, Disable, Remove
		}
	}
}
