package io.intino.ness.master.model;

import io.intino.ness.master.Datamart;
import io.intino.ness.master.reflection.AttributeDefinition;
import io.intino.ness.master.reflection.ConceptDefinition;

import java.util.List;
import java.util.Objects;

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
		default String name() {return getDefinition().name();}
		default Class<?> type() {return getDefinition().type();}
		Value value();
		Attribute addChangeListener(ChangeListener listener);
		AttributeDefinition getDefinition();

		class Value {
			private final Object value;

			public Value(Object value) {
				this.value = value;
			}

			public Object get() {
				return value;
			}

			public <T> T as(Class<T> type) {
				return type.cast(value);
			}

			@SuppressWarnings("unchecked")
			public <T> T as() {
				return (T) (value);
			}

			public String asString() {
				return as(String.class);
			}

			@Override
			public boolean equals(Object o) {
				if (this == o) return true;
				if (o == null || getClass() != o.getClass()) return false;
				Value value1 = (Value) o;
				return Objects.equals(value, value1.value);
			}

			@Override
			public int hashCode() {
				return Objects.hash(value);
			}

			@Override
			public String toString() {
				return String.valueOf(get());
			}
		}

		@FunctionalInterface
		interface ChangeListener {
			void onValueChange(Value oldValue, Value newValue);
		}
	}
}
