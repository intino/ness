package io.intino.master.data;


import io.intino.master.data.validation.RecordValidator.TripleRecord;

import java.util.Map;

public interface RecordTransformer {

	static RecordTransformer dummy() {
		return r -> r;
	}

	Map<String, TripleRecord> transform(Map<String, TripleRecord> records);
}
