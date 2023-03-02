package io.intino.ness.master.reflection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface ConceptDefinition {

	char NAME_SEPARATOR = '.';

	String fullName();

	default String name() {
		String fullName = fullName();
		int nameBegin = fullName.lastIndexOf(NAME_SEPARATOR);
		return nameBegin < 0 ? fullName : fullName.substring(nameBegin);
	}

	default List<AttributeDefinition> attributes() {
		List<AttributeDefinition> attributes = parent().map(ConceptDefinition::attributes).orElse(new ArrayList<>());
		attributes.addAll(declaredAttributes());
		return attributes;
	}

	List<AttributeDefinition> declaredAttributes();

	default Optional<AttributeDefinition> attribute(String name) {
		return attributes().stream().filter(attr -> attr.name().equals(name)).findFirst();
	}

	default Optional<AttributeDefinition> declaredAttribute(String name) {
		return declaredAttributes().stream().filter(attr -> attr.name().equals(name)).findFirst();
	}

	Optional<ConceptDefinition> parent();

	default List<ConceptDefinition> ancestors() {
		List<ConceptDefinition> ancestors = new ArrayList<>();
		Optional<ConceptDefinition> parent = parent();
		while(parent.isPresent()) {
			ancestors.add(parent.get());
			parent = parent.get().parent();
		}
		Collections.reverse(ancestors);
		return ancestors;
	}

	List<ConceptDefinition> descendants();

	default boolean isAncestorOf(ConceptDefinition other) {
		return ancestors().stream().anyMatch(a -> a.fullName().equals(other.fullName()));
	}

	default boolean isDescendantOf(ConceptDefinition other) {
		return descendants().stream().anyMatch(a -> a.fullName().equals(other.fullName()));
	}

	Class<?> javaClass();
}
