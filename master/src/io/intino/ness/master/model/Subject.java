package io.intino.ness.master.model;

import io.intino.ness.master.reflection.SubjectDefinition;

public non-sealed interface Subject extends Concept {

	String id();

	boolean enabled();

	@Override
	SubjectDefinition getDefinition();
}