package io.intino.ness.master.data.validation;

import java.util.List;
import java.util.stream.Stream;

public interface FieldValidator {

	static FieldValidator none() {return (v, r, s) -> Stream.empty();}

	Stream<Issue> validate(List<RecordValidator.TripletRecord.Value> values, RecordValidator.TripletRecord record, TripletRecordStore store);
}
