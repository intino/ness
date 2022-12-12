package io.intino.ness.master.data.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class TripletValidationLayer {

	private final List<TripletValidator> validators = new ArrayList<>(2);

	public Stream<Issue> validate(String tripleLine, TripletSource source) {
		return validators.stream().flatMap(validator -> validate(validator, tripleLine, source));
	}

	private Stream<Issue> validate(TripletValidator validator, String tripleLine, TripletSource source) {
		return validator.validate(tripleLine, source).filter(Objects::nonNull).peek(issue -> issue.scope(ValidationLayers.Scope.TRIPLES));
	}

	public TripletValidationLayer addValidator(TripletValidator validator) {
		if (validator == null) return this;
		validators.add(validator);
		return this;
	}
}
