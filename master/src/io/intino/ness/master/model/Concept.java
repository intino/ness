package io.intino.ness.master.model;

import io.intino.ness.master.Datamart;
import io.intino.ness.master.reflection.AttributeDefinition;
import io.intino.ness.master.reflection.ConceptDefinition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public sealed interface Concept permits Entity, Struct {

	Datamart datamart();

	Attribute attribute(String name);

	List<Attribute> attributes();

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

			@SuppressWarnings("unchecked")
			public <T> T as(Class<T> type) {
				if(value == null) return null;
				if(value.getClass() == String.class && StringParsers.containsKey(type)) {
					return (T) StringParsers.get(type).apply(value.toString());
				}
				return (T) value;
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

			private static final Map<Class<?>, Function<String, Object>> StringParsers = new HashMap<>() {{
				put(byte.class, Byte::parseByte);
				put(short.class, Short::parseShort);
				put(int.class, Integer::parseInt);
				put(long.class, Long::parseLong);
				put(float.class, Float::parseFloat);
				put(double.class, Double::parseDouble);
				put(boolean.class, Boolean::parseBoolean);
				put(Byte.class, Byte::parseByte);
				put(Short.class, Short::parseShort);
				put(Integer.class, Integer::parseInt);
				put(Long.class, Long::parseLong);
				put(Float.class, Float::parseFloat);
				put(Double.class, Double::parseDouble);
				put(Boolean.class, Boolean::parseBoolean);
			}};
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
