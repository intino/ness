package io.intino.ness.master.reflection;

import io.intino.ness.master.model.Concept;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface AttributeDefinition {

	String name();

	Class<?> type();

	default Optional<Concept.Attribute.Value> value(Concept owner) {
		Concept.Attribute attribute = owner.attribute(name());
		return attribute == null ? Optional.empty() : Optional.of(attribute.value());
	}

	default boolean isCollection() {
		return !parameters().isEmpty();
	}

	default List<ParameterDefinition> parameters() {
		return Collections.emptyList();
	}

	interface ParameterDefinition {

		Optional<ConceptDefinition<?>> asConceptDefinition();

		Class<?> javaClass();
	}
}
