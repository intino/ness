package io.intino.ness.master.data.validation.validators;

import io.intino.ness.master.data.validation.Issue;
import io.intino.ness.master.data.validation.TripleSource;
import io.intino.ness.master.data.validation.TripleValidator;
import io.intino.ness.master.model.Triplet;

import java.util.stream.Stream;

import static io.intino.ness.master.core.Master.NONE_TYPE;
import static io.intino.ness.master.data.validation.Issue.Type.SUBJECT_WITHOUT_TYPE;
import static io.intino.ness.master.model.Triplet.typeOf;
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
		Triplet triplet = new Triplet(tripleLine);
		return hasNoType(triplet.subject())
				? Stream.of(Issue.create(level, SUBJECT_WITHOUT_TYPE, "Triple (" + triplet.subject() + ") subject must have a type").source(source))
				: Stream.empty();
	}

	private boolean hasNoType(String subject) {
		final String type = typeOf(subject);
		return type == null || type.equals(NONE_TYPE);
	}
}
