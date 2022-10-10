package io.intino.master.data.validation;

import java.util.*;
import java.util.stream.Stream;

import static io.intino.master.model.Triple.typeOf;

public interface RecordValidator {

	static RecordValidator none() { return (r, s) -> Stream.empty(); }

	Stream<Issue> validate(TripleRecord record, TripleRecordStore store);

	class TripleRecord {

		private final String id;
		private final Map<String, List<Value>> attributes = new LinkedHashMap<>();
		private TripleSource source;

		public TripleRecord(String id) {
			this.id = id;
		}

		public void add(String predicate, Value value) {
			attributes.computeIfAbsent(predicate, k -> new ArrayList<>()).add(value);
		}

		public String id() {
			return id;
		}

		public String type() {
			return typeOf(id);
		}

		public Map<String, List<Value>> attributes() {
			return attributes;
		}

		public List<Value> get(String attribute) {
			return attributes.getOrDefault(attribute, Collections.emptyList());
		}

		public TripleSource source() {
			return source;
		}

		public TripleRecord source(TripleSource source) {
			this.source = source;
			return this;
		}

		public static class Value {

			private final String value;
			private TripleSource source;

			public Value(String value) {
				this.value = value;
			}

			public boolean isEmpty() {
				return value == null;
			}

			public String get() {
				return value;
			}

			public TripleSource source() {
				return source;
			}

			public Value source(TripleSource source) {
				this.source = source;
				return this;
			}

			@Override
			public String toString() {
				return value;
			}
		}
	}
}
