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

	void addChangeListener(ChangeListener listener);

	ConceptDefinition<?> getDefinition();

	@SuppressWarnings({"unchecked", "rawtypes"})
	default boolean instanceOf(ConceptDefinition definition) {
		ConceptDefinition myDefinition = getDefinition();
		return myDefinition.equals(definition) || myDefinition.isDescendantOf(definition);
	}

	interface Attribute {

		default String name() {return getDefinition().name();}

		default Class<?> type() {return getDefinition().type();}

		Value value();

		void addChangeListener(ChangeListener listener);

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

			@Override
			public boolean equals(Object o) {
				if (this == o) return true;
				if (o == null || getClass() != o.getClass()) return false;
				return Objects.equals(value, ((Value) o).value);
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

	@FunctionalInterface
	interface ChangeListener {
		void onChange(Concept concept, Attribute attribute, Attribute.Value oldValue);
	}
}
