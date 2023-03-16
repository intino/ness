package io.intino.ness.master.reflection;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface ConceptDefinition<T extends ConceptDefinition<T>> {

	char NAME_SEPARATOR = '.';

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
		return ancestors().stream().anyMatch(a -> a.fullName().equals(other.fullName()));
	}

	default boolean isDescendantOf(T other) {
		return descendants().stream().anyMatch(a -> a.fullName().equals(other.fullName()));
	}

	Class<?> javaClass();
}
