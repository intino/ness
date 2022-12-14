package io.intino.ness.master.serialization;

import io.intino.ness.master.model.Triplet;
import io.intino.ness.master.model.TripletRecord;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class SeparatorMasterSerializer implements MasterSerializer {

	private static final String FIELD_SEPARATOR = "\n";

	private final String separator;

	public SeparatorMasterSerializer(String separator) {
		if (separator == null || separator.isEmpty())
			throw new IllegalArgumentException("Separator cannot be null nor empty");
		this.separator = separator;
	}

	@Override
	public String serialize(TripletRecord record) {
		return record.triplets()
				.map(triplet -> String.join(separator, triplet.attributes()))
				.collect(Collectors.joining(FIELD_SEPARATOR));
	}

	@Override
	public TripletRecord deserialize(String str) {
		Map<String, Triplet> triplets = Arrays.stream(str.split(FIELD_SEPARATOR))
				.map(line -> line.split(separator, -1))
				.map(Triplet::new)
				.collect(Collectors.toMap(Triplet::predicate, Function.identity(), (t1, t2) -> t2));
		return new TripletRecord(triplets);
	}

	public static final class Csv extends SeparatorMasterSerializer {

		public Csv() {
			super(",");
		}

		@Override
		public String name() {
			return "csv";
		}
	}

	public static final class Tsv extends SeparatorMasterSerializer {

		public Tsv() {
			super("\t");
		}

		@Override
		public String name() {
			return "tsv";
		}
	}
}
