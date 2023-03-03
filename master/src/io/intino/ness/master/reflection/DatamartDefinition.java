package io.intino.ness.master.reflection;

import io.intino.ness.master.Datamart;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface DatamartDefinition {

	String name();

	Datamart.Scale scale();

	default List<ConceptDefinition<?>> concepts() {
		List<ConceptDefinition<?>> concepts = new ArrayList<>(entities());
		concepts.addAll(structs());
		return concepts;
	}

	Query<EntityDefinition> entities();

	Query<StructDefinition> structs();

	default Optional<ConceptDefinition<?>> concept(String name) {
		return concepts().stream().filter(e -> e.name().equals(name)).findFirst();
	}

	default Optional<EntityDefinition> entity(String name) {
		return entities().stream().filter(e -> e.name().equals(name)).findFirst();
	}

	default Optional<StructDefinition> struct(String name) {
		return structs().stream().filter(e -> e.name().equals(name)).findFirst();
	}

	class Query<T extends ConceptDefinition<T>> extends AbstractList<T> {

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
