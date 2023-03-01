package io.intino.ness.master;

import io.intino.ness.master.model.Entity;
import io.intino.ness.master.reflection.DatamartDefinition;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Datamart {

	default String name() {return getDefinition().name();}

	int size();

	<T extends Entity> T get(String id);

	<T extends Entity> Stream<T> entities();

	default Map<String, Entity> toMap() {
		return entities().collect(Collectors.toMap(Entity::id, Function.identity()));
	}

	DatamartDefinition getDefinition();
}
