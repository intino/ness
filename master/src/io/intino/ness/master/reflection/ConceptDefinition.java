package io.intino.ness.master.reflection;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface ConceptDefinition<T extends ConceptDefinition<T>> {

	char NAME_SEPARATOR = '.';
	String NAME_SEPARATOR_REGEX = "\\.";

	String fullName();

	default String name() {
		String fullName = fullName();
		int nameBegin = fullName.lastIndexOf(NAME_SEPARATOR);
		return nameBegin < 0 ? fullName : fullName.substring(nameBegin + 1);
	}

	default List<AttributeDefinition> attributes() {
		return Stream.of(
				parent().map(ConceptDefinition::attributes).stream().flatMap(Collection::stream),
				declaredAttributes().stream()
		).flatMap(Function.identity()).collect(Collectors.toList());
	}

	List<AttributeDefinition> declaredAttributes();

	default Optional<AttributeDefinition> attribute(String name) {
		return attributes().stream().filter(attr -> attr.name().equals(name)).findFirst();
	}

	default Optional<AttributeDefinition> declaredAttribute(String name) {
		return declaredAttributes().stream().filter(attr -> attr.name().equals(name)).findFirst();
	}

	Optional<T> parent();

	default List<T> ancestors() {
		List<T> ancestors = new ArrayList<>();
		Optional<T> parent = parent();
		while(parent.isPresent()) {
			ancestors.add(parent.get());
			parent = parent.get().parent();
		}
		Collections.reverse(ancestors);
		return ancestors;
	}

	List<T> descendants();

	default boolean isAncestorOf(T other) {
		final String me = this.name();
		String[] ancestors = other.fullName().split(NAME_SEPARATOR_REGEX);
		for(int i = 0;i < ancestors.length - 1;i++) {
			if(ancestors[i].equals(me)) return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	default boolean isDescendantOf(T other) {
		return other.isAncestorOf((T) this);
	}

	Class<?> javaClass();
}
