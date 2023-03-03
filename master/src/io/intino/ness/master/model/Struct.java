package io.intino.ness.master.model;

import io.intino.ness.master.reflection.StructDefinition;

public interface Struct extends Concept {

	@Override
	StructDefinition getDefinition();
}
