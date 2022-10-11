package io.intino.ness.master.data.validation.validators;

import io.intino.ness.master.data.validation.Issue;
import io.intino.ness.master.data.validation.TripleSource;
import io.intino.ness.master.data.validation.TripleValidator;
import io.intino.ness.master.model.Triplet;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static io.intino.ness.master.data.validation.Issue.Type.SYNTAX_ERROR;

public class SyntaxTripleValidator implements TripleValidator {

	private final Issue.Level level;

	public SyntaxTripleValidator() {
		this(Issue.Level.Error);
	}

	public SyntaxTripleValidator(Issue.Level level) {
		this.level = level;
	}

	@Override
	public Stream<Issue> validate(String tripleLine, TripleSource source) {
		String[] rawTriple = tripleLine.split(Triplet.TRIPLET_SEPARATOR);
		if (rawTriple.length != 3)
			return Stream.of(Issue.create(level, SYNTAX_ERROR, "Triple (" + tripleLine + ") is malformed: it should have 3 fields separated by TAB, but has " + rawTriple.length).source(source));

		Triplet triplet = new Triplet(tripleLine);
		List<Issue> issues = new ArrayList<>(2);

		if (isNullOrBlank(triplet.subject()))
			issues.add(Issue.create(level, SYNTAX_ERROR, "Triple (" + triplet.subject() + ") subject cannot be null nor blank").source(source));
		if (isNullOrBlank(triplet.predicate()))
			issues.add(Issue.create(level, SYNTAX_ERROR, "Triple (" + triplet.subject() + ") predicate cannot be null nor blank").source(source));
		if (triplet.predicate().contains(" "))
			issues.add(Issue.create(level, SYNTAX_ERROR, "Triple (" + triplet.subject() + ") predicates cannot contain spaces").source(source));

		return issues.stream();
	}

	private static boolean isNullOrBlank(String s) {
		return s == null || s.isBlank();
	}
}
