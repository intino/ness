package io.intino.ness.master.model;

import io.intino.ness.master.reflection.EntityDefinition;

public non-sealed interface Entity extends Concept {

	String id();

	boolean enabled();

	@Override
	EntityDefinition getDefinition();
}