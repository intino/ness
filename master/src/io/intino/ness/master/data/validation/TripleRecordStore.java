package io.intino.ness.master.data.validation;

import java.util.stream.Stream;

public interface TripleRecordStore {

	RecordValidator.TripleRecord get(String id);

	Stream<RecordValidator.TripleRecord> stream();
}