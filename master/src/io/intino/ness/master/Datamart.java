package io.intino.ness.master;

import io.intino.ness.master.model.Subject;
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

	<T extends Subject> T get(String id);

	Stream<? extends Subject> subjects();

	default Map<String, Subject> toMap() {
		return subjects().collect(Collectors.toMap(Subject::id, Function.identity()));
	}

	void addSubjectListener(SubjectListener listener);

	DatamartDefinition getDefinition();

	Translator translator();

	interface SubjectListener {

		void onCreate(Subject entity);

		void onUpdate(Subject entity);

		void onRemove(Subject entity);

		interface OnCreate extends SubjectListener {
			@Override
			default void onUpdate(Subject entity) {
			}

			@Override
			default void onRemove(Subject entity) {
			}
		}

		interface OnUpdate extends SubjectListener {
			@Override
			default void onCreate(Subject entity) {
			}

			@Override
			default void onRemove(Subject entity) {
			}
		}

		interface OnRemove extends SubjectListener {
			@Override
			default void onCreate(Subject entity) {
			}

			@Override
			default void onUpdate(Subject entity) {
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
