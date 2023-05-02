package io.intino.ness.master.model;

import io.intino.ness.master.Datamart;

public interface Node<T> {

	Datamart datamart();

	T get();

	@FunctionalInterface
	interface ChangeListener {
		void onChange(Object event);
	}
}
