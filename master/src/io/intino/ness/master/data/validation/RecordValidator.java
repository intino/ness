package io.intino.ness.master.data.validation;

import io.intino.ness.master.model.Triplet;

import java.util.*;
import java.util.stream.Stream;

public interface RecordValidator {

	String LIST_SEPARATOR = ";";
	String MAP_KEY_VALUE_SEPARATOR = "=";
	String STRUCT_FIELD_SEPARATOR = ",";

	static RecordValidator none() {
		return (r, s) -> Stream.empty();
	}

	Stream<Issue> validate(TripletRecord record, TripletRecordStore store);

	class TripletRecord {

		private final String id;
		private final Map<String, List<Value>> attributes = new LinkedHashMap<>();
		private TripletSource source;

		public TripletRecord(String id) {
			this.id = id;
		}

		public void add(String predicate, Value value) {
			attributes.computeIfAbsent(predicate, k -> new ArrayList<>()).add(value);
		}

		public String id() {
			return id;
		}

		public String type() {
			return Triplet.typeOf(id);
		}

		public Map<String, List<Value>> attributes() {
			return attributes;
		}

		public List<Value> get(String attribute) {
			return attributes.getOrDefault(attribute, Collections.emptyList());
		}

		public TripletSource source() {
			return source;
		}

		public TripletRecord source(TripletSource source) {
			this.source = source;
			return this;
		}

		public static class Value {

			private final String value;
			private TripletSource source;

			public Value(String value) {
				this.value = value;
			}

			public boolean isEmpty() {
				return value == null;
			}

			public String get() {
				return value;
			}

			public TripletSource source() {
				return source;
			}

			public Value source(TripletSource source) {
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
