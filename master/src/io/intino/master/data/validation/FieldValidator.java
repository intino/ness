package io.intino.master.data.validation;

import java.util.List;
import java.util.stream.Stream;

public interface FieldValidator {

	static FieldValidator none() {return (v, r, s) -> Stream.empty();}

	Stream<Issue> validate(List<RecordValidator.TripleRecord.Value> values, RecordValidator.TripleRecord record, TripleRecordStore store);
}
