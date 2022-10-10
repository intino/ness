package io.intino.ness.master.data;


import io.intino.ness.master.data.validation.RecordValidator.TripleRecord;

import java.util.Map;

public interface RecordTransformer {

	static RecordTransformer dummy() {
		return r -> r;
	}

	Map<String, TripleRecord> transform(Map<String, TripleRecord> records);
}
