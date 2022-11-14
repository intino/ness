package io.intino.ness.master.data;

import io.intino.ness.master.model.Entity;

public interface EntityListener<T extends Entity> {

	void notify(Event<T> event);

	static <E extends Entity> EntityListener<E> onCreate(EntityListener<E> listener) {
		return event -> { if(event.type() == Event.Type.Create) listener.notify(event); };
	}

	static <E extends Entity> EntityListener<E> onUpdate(EntityListener<E> listener) {
		return event -> { if(event.type() == Event.Type.Update) listener.notify(event); };
	}

	static <E extends Entity> EntityListener<E> onRemove(EntityListener<E> listener) {
		return event -> { if(event.type() == Event.Type.Remove) listener.notify(event); };
	}

	interface Event<T extends Entity> {

		Type type();

		T entity();

		enum Type {
			Create, Update, Remove
		}
	}
}
