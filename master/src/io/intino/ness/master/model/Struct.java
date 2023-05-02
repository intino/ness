package io.intino.ness.master.model;

import io.intino.ness.master.reflection.StructDefinition;

public non-sealed interface Struct extends Concept {

	@Override
	StructDefinition getDefinition();
}
