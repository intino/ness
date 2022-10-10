package io.intino.master.serialization;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class MasterSerializers {

	public static final class Standard {

		public static final String TSV = "tsv";
		public static final String CSV = "csv";

		public static String getDefault() {
			return TSV;
		}
	}

	private static final Map<String, Supplier<MasterSerializer>> Serializers = new ConcurrentHashMap<>();
	static {
		Serializers.put(Standard.TSV, SeparatorMasterSerializer.Tsv::new);
		Serializers.put(Standard.CSV, SeparatorMasterSerializer.Csv::new);
		Serializers.put("default", SeparatorMasterSerializer.Tsv::new);
	}

	public static MasterSerializer getDefault() {
		return Serializers.get("default").get();
	}

	public static MasterSerializer getOrDefault(String name) {
		if(name == null) return null;
		MasterSerializer serializer = get(name);
		return serializer != null ? serializer : getDefault();
	}

	public static MasterSerializer get(String name) {
		if(name == null) return null;
		return Serializers.getOrDefault(name.toLowerCase(), MasterSerializers::getNull).get();
	}

	public static void setSerializer(String name, Supplier<MasterSerializer> serializerSupplier) {
		if(serializerSupplier == null) throw new NullPointerException("Serializer supplier cannot be null");
		if(name.equalsIgnoreCase("default")) throw new IllegalArgumentException("Cannot set the default serializer");
		Serializers.put(name, serializerSupplier);
	}

	private static MasterSerializer getNull() {
		return null;
	}
}
