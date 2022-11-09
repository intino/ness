package io.intino.ness.master.data.validation;

import java.util.stream.Stream;

public interface TripletRecordStore {

	RecordValidator.TripletRecord get(String id);

	Stream<RecordValidator.TripletRecord> stream();
}
