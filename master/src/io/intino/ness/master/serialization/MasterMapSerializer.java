package io.intino.ness.master.serialization;

import java.io.IOException;
import java.util.Map;

public interface MasterMapSerializer {

	String name();
	byte[] serialize(Map<String, String> map) throws Exception;
	Map<String, String> deserialize(byte[] bytes) throws Exception;

	static MasterMapSerializer getDefault() {
		return new Snappy();
	}

	static MasterMapSerializer get(String name) {
		switch(name) {
			case "SNAPPY": return new Snappy();
			case "JSON": return new Json();
		}
		return null;
	}

	class Snappy implements MasterMapSerializer {

		@Override
		public String name() {
			return "SNAPPY";
		}

		@Override
		public byte[] serialize(Map<String, String> map) throws IOException {
			return org.xerial.snappy.Snappy.compress(new Json().serialize(map));
		}

		@Override
		public Map<String, String> deserialize(byte[] bytes) throws IOException {
			return new Json().deserialize(org.xerial.snappy.Snappy.uncompress(bytes));
		}
	}

	class Json implements MasterMapSerializer {

		@Override
		public String name() {
			return "JSON";
		}

		@Override
		public byte[] serialize(Map<String, String> map) {
			return io.intino.alexandria.Json.toJson(map).getBytes();
		}

		@Override
		@SuppressWarnings("unchecked")
		public Map<String, String> deserialize(byte[] bytes) {
			return io.intino.alexandria.Json.fromJson(new String(bytes), Map.class);
		}
	}
}
