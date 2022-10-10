package io.intino.master.serialization;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class SeparatorMasterSerializer implements MasterSerializer {

	private static final String FIELD_SEPARATOR = "\n";

	private final String separator;

	public SeparatorMasterSerializer(String separator) {
		if(separator == null || separator.isEmpty()) throw new IllegalArgumentException("Separator cannot be null nor empty");
		this.separator = separator;
	}

	@Override
	public String serialize(Map<String, String> record) {
		return record.entrySet().stream().map(field -> field.getKey() + separator + field.getValue()).collect(Collectors.joining(FIELD_SEPARATOR));
	}

	@Override
	public Map<String, String> deserialize(String str) {
		return Arrays.stream(str.split(FIELD_SEPARATOR)).map(line -> line.split(separator)).collect(Collectors.toMap(f -> f[0], f -> f[1]));
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
