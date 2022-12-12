package io.intino.ness.master.data;

import io.intino.ness.master.model.TripletRecord;
import io.intino.ness.master.serialization.MasterSerializer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public interface MasterTripletsDigester {

	static MasterTripletsDigester createDefault() {
		return new DefaultTripletsDigester();
	}

	Result load(EntityLoader entityLoader, MasterSerializer serializer) throws Exception;

	interface Result {
		static WritableResult create() {
			return new WritableResult();
		}

		Stats stats();

		Map<String, TripletRecord> records();

		class Stats {
			public static final String TRIPLETS_READ = "Triplets read";
			public static final String FILES_READ = "Files read";
			public static final String LINES_READ = "Lines read";

			private final Map<String, Object> statsMap = new LinkedHashMap<>();

			public void put(String key, Object value) {
				statsMap.put(key, value);
			}

			public void increment(String key) {
				statsMap.compute(key, (k, v) -> v == null ? 1 : (int) v + 1);
			}

			@SuppressWarnings("unchecked")
			public <T> T get(String key) {
				return (T) statsMap.get(key);
			}

			@SuppressWarnings("unchecked")
			public <T> T getOrDefault(String key, T defValue) {
				return (T) statsMap.getOrDefault(key, defValue);
			}

			public String getString(String key, Object defValue) {
				return String.valueOf(statsMap.getOrDefault(key, defValue));
			}

			public Set<Map.Entry<String, Object>> entrySet() {
				return statsMap.entrySet();
			}

			public Map<String, Object> map() {
				return statsMap;
			}
		}
	}

	class WritableResult implements Result {

		private final Stats stats = new Stats();
		private Map<String, TripletRecord> records = new HashMap<>();

		@Override
		public Stats stats() {
			return stats;
		}

		@Override
		public Map<String, TripletRecord> records() {
			return records;
		}

		public WritableResult records(Map<String, TripletRecord> records) {
			this.records = records;
			return this;
		}
	}
}
