package io.intino.master.data.validation;

import java.util.stream.Stream;

public interface TripleValidator {

	Stream<Issue> validate(String tripleLine, TripleSource source);
}
