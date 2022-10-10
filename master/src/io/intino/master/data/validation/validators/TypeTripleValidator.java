package io.intino.master.data.validation.validators;

import io.intino.master.data.validation.Issue;
import io.intino.master.data.validation.TripleSource;
import io.intino.master.data.validation.TripleValidator;
import io.intino.master.model.Triple;

import java.util.stream.Stream;

import static io.intino.master.core.Master.NONE_TYPE;
import static io.intino.master.data.validation.Issue.Type.SUBJECT_WITHOUT_TYPE;
import static io.intino.master.model.Triple.typeOf;
import static java.util.Objects.requireNonNull;

public class TypeTripleValidator implements TripleValidator {

	private final Issue.Level level;

	public TypeTripleValidator() {
		this(Issue.Level.Error);
	}

	public TypeTripleValidator(Issue.Level level) {
		this.level = requireNonNull(level);
	}

	@Override
	public Stream<Issue> validate(String tripleLine, TripleSource source) {
		Triple triple = new Triple(tripleLine);
		return hasNoType(triple.subject())
				? Stream.of(Issue.create(level, SUBJECT_WITHOUT_TYPE, "Triple (" + triple.subject() + ") subject must have a type").source(source))
				: Stream.empty();
	}

	private boolean hasNoType(String subject) {
		final String type = typeOf(subject);
		return type == null || type.equals(NONE_TYPE);
	}
}
