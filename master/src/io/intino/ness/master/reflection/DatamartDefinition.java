package io.intino.ness.master.reflection;

import java.util.AbstractList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface DatamartDefinition {

	String name();

	Scale scale();

	Query<EntityDefinition> entities();

	Query<StructDefinition> structs();

	default Optional<EntityDefinition> entity(String name) {
		return entities().stream().filter(e -> e.name().equals(name)).findFirst();
	}

	default Optional<StructDefinition> struct(String name) {
		return structs().stream().filter(e -> e.name().equals(name)).findFirst();
	}

	enum Scale {
		Year, Month, Week, Day
	}

	class Query<T extends ConceptDefinition> extends AbstractList<T> {

		private final List<T> source;

		public Query(List<T> source) {
			this.source = source;
		}

		public Query<T> instanceOf(T ancestor) {
			return new Query<>(source.stream().filter(ancestor::isAncestorOf).collect(Collectors.toList()));
		}

		public Query<T> of(T definition) {
			return new Query<>(source.stream().filter(definition::equals).collect(Collectors.toList()));
		}

		@Override
		public T get(int index) {
			return source.get(index);
		}

		@Override
		public int size() {
			return source.size();
		}
	}
}
