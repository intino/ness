package io.intino.ness.master;

import io.intino.ness.master.model.Entity;
import io.intino.ness.master.reflection.DatamartDefinition;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Datamart {

	default String name() {
		return getDefinition().name();
	}

	default Scale scale() {
		return getDefinition().scale();
	}

	int size();

	<T extends Entity> T get(String id);

	Stream<Entity> entities();

	default Map<String, Entity> toMap() {
		return entities().collect(Collectors.toMap(Entity::id, Function.identity()));
	}

	void addEntityListener(EntityListener listener);

	DatamartDefinition getDefinition();

	Translator translator();

	interface EntityListener {

		void onCreate(Entity entity);

		void onUpdate(Entity entity);

		void onRemove(Entity entity);

		interface OnCreate extends EntityListener {
			@Override
			default void onUpdate(Entity entity) {
			}

			@Override
			default void onRemove(Entity entity) {
			}
		}

		interface OnUpdate extends EntityListener {
			@Override
			default void onCreate(Entity entity) {
			}

			@Override
			default void onRemove(Entity entity) {
			}
		}

		interface OnRemove extends EntityListener {
			@Override
			default void onCreate(Entity entity) {
			}

			@Override
			default void onUpdate(Entity entity) {
			}
		}
	}

	enum Scale {
		Year, Month, Week, Day, None
	}

	interface Translator {
		Optional<String> translate(String text, String language);

		class Identity implements Translator {

			@Override
			public Optional<String> translate(String text, String language) {
				return Optional.of(text);
			}
		}

	}
}
