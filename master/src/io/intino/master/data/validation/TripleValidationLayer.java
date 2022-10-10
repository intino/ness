package io.intino.master.data.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

public class TripleValidationLayer {

	private final List<TripleValidator> validators = new ArrayList<>(2);

	public Stream<Issue> validate(String tripleLine, TripleSource source) {
		return validators.stream().flatMap(validator -> validate(validator, tripleLine, source));
	}

	private Stream<Issue> validate(TripleValidator validator, String tripleLine, TripleSource source) {
		return validator.validate(tripleLine, source).filter(Objects::nonNull).peek(issue -> issue.scope(ValidationLayers.Scope.TRIPLES));
	}

	public TripleValidationLayer addValidator(TripleValidator validator) {
		if(validator == null) return this;
		validators.add(validator);
		return this;
	}
}
