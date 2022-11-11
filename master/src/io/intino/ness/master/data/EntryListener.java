package io.intino.ness.master.data;

import io.intino.ness.master.model.Entity;

import java.time.Instant;

public interface EntryListener<T extends Entity> {

	void notify(Event<T> event);

	static <E extends Entity> EntryListener<E> onCreate(EntryListener<E> listener) {
		return event -> { if(event.type() == Event.Type.Create) listener.notify(event); };
	}

	static <E extends Entity> EntryListener<E> onUpdate(EntryListener<E> listener) {
		return event -> { if(event.type() == Event.Type.Update) listener.notify(event); };
	}

	static <E extends Entity> EntryListener<E> onRemove(EntryListener<E> listener) {
		return event -> { if(event.type() == Event.Type.Remove) listener.notify(event); };
	}

	interface Event<T extends Entity> {

		Type type();

		T entity();

		String author();

		Instant ts();

		enum Type {
			Create, Update, Remove
		}
	}
}
