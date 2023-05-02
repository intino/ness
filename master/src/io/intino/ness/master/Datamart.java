package io.intino.ness.master;

import io.intino.ness.master.model.Entity;
import io.intino.ness.master.model.Node;
import io.intino.ness.master.reflection.DatamartDefinition;
import io.intino.sumus.chronos.Reel;
import io.intino.sumus.chronos.Timeline;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Datamart {

	default String name() {return getDefinition().name();}

	default Scale scale() {return getDefinition().scale();}

	int size();

	Node<Timeline> timeline(String id);

	Node<Reel> reel(String id);

	<T extends Entity> T get(String id);

	Stream<Entity> entities();

	default Map<String, Entity> toMap() {
		return entities().collect(Collectors.toMap(Entity::id, Function.identity()));
	}

	void addEntityListener(EntityListener listener);

	DatamartDefinition getDefinition();

	interface EntityListener {

		void onCreate(Entity entity);
		void onUpdate(Entity entity);
		void onRemove(Entity entity);

		interface OnCreate extends EntityListener {
			@Override
			default void onUpdate(Entity entity) {}
			@Override
			default void onRemove(Entity entity) {}
		}

		interface OnUpdate extends EntityListener {
			@Override
			default void onCreate(Entity entity) {}
			@Override
			default void onRemove(Entity entity) {}
		}

		interface OnRemove extends EntityListener {
			@Override
			default void onCreate(Entity entity) {}
			@Override
			default void onUpdate(Entity entity) {}
		}
	}

	enum Scale {
		Year, Month, Week, Day, None
	}
}
