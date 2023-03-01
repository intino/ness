package io.intino.ness.master.model;

import io.intino.ness.master.Datamart;
import io.intino.ness.master.reflection.AttributeDefinition;
import io.intino.ness.master.reflection.ConceptDefinition;

import java.util.List;

public interface Concept {

	Attribute attribute(String name);

	List<Attribute> attributes();

	Datamart datamart();

	ConceptDefinition getDefinition();

	default boolean instanceOf(ConceptDefinition definition) {
		ConceptDefinition myDefinition = getDefinition();
		return myDefinition.equals(definition) || myDefinition.isDescendantOf(definition);
	}

	interface Attribute {
		default String name() {return definition().name();}
		default Class<?> type() {return definition().type();}
		Value value();
		Attribute addChangeListener(ChangeListener listener);
		AttributeDefinition definition();

		@SuppressWarnings("unchecked")
		class Value {
			private final Object value;
			public Value(Object value) {
				this.value = value;
			}
			public Object get() {return value;}
			public <T> T as(Class<T> type) { return (T) value;};
		}

		@FunctionalInterface
		interface ChangeListener {
			void onValueChange(Value oldValue, Value newValue);
		}
	}
}
