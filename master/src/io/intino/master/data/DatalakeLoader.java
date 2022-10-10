package io.intino.master.data;

import io.intino.master.model.TripleRecord;
import io.intino.master.serialization.MasterSerializer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface DatalakeLoader {

	static DatalakeLoader createDefault() {
		return new DefaultDatalakeLoader();
	}

	LoadResult load(File rootDirectory, MasterSerializer serializer);

	interface LoadResult {
		static WritableLoadResult create() {
			return new WritableLoadResult();
		}
		List<File> filesRead();
		long linesRead();
		long triplesRead();
		Map<String, TripleRecord> records();
	}

	class WritableLoadResult implements LoadResult {

		private final List<File> filesRead = new ArrayList<>();
		private long linesRead;
		private long triplesRead;
		private int numRecords;
		private Map<String, TripleRecord> records = new HashMap<>();

		public List<File> filesRead() {
			return filesRead;
		}

		@Override
		public long linesRead() {
			return linesRead;
		}

		public WritableLoadResult linesRead(long linesRead) {
			this.linesRead = linesRead;
			return this;
		}

		@Override
		public long triplesRead() {
			return triplesRead;
		}

		public WritableLoadResult triplesRead(long triplesRead) {
			this.triplesRead = triplesRead;
			return this;
		}

		@Override
		public Map<String, TripleRecord> records() {
			return records;
		}

		public WritableLoadResult records(Map<String, TripleRecord> records) {
			this.records = records;
			return this;
		}
	}
}
