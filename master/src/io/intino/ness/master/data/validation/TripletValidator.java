package io.intino.ness.master.data.validation;

import java.util.stream.Stream;

public interface TripletValidator {

	Stream<Issue> validate(String tripleLine, TripletSource source);
}
