package io.intino.ness.master.data.validation.readers;

import io.intino.ness.master.data.validation.RecordValidator;
import io.intino.ness.master.data.validation.TripletSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public interface ValidationTripletRecordReader {

	Stream<RecordValidator.TripletRecord> records() throws IOException;

	static ValidationTripletRecordReader ofFile(File file) {
		return new FileValidationTripletRecordReader(file);
	}

	class FileValidationTripletRecordReader implements ValidationTripletRecordReader {

		private final File file;

		public FileValidationTripletRecordReader(File file) {
			this.file = file;
		}

		@Override
		public Stream<RecordValidator.TripletRecord> records() throws IOException {
			Map<String, RecordValidator.TripletRecord> records = new HashMap<>();
			try(Stream<String> lines = Files.lines(file.toPath())) {
				readTripletRecordsFromLines(records, lines);
			}
			return records.values().stream();
		}

		private void readTripletRecordsFromLines(Map<String, RecordValidator.TripletRecord> records, Stream<String> lines) {
			AtomicInteger lineNumber = new AtomicInteger();
			lines.peek(l -> lineNumber.incrementAndGet())
					.filter(line -> !line.isBlank())
					.map(line -> line.split("\t"))
					.forEach(t -> {
						String subject = t[0].trim();
						String predicate = t[1].trim();
						String value = t[2].trim();
						RecordValidator.TripletRecord record = records.computeIfAbsent(subject, k -> new RecordValidator.TripletRecord(subject));
						record.add(predicate, new RecordValidator.TripletRecord.Value(value).source(TripletSource.ofFile(file.getAbsolutePath(), lineNumber.get())));
					});
		}

	}
}
