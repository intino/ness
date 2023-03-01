package io.intino.datahub.master.serialization;

import io.intino.alexandria.Json;
import io.intino.datahub.master.MasterDatamart;
import io.intino.datahub.master.datamarts.messages.MapMessageMasterDatamart;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.Map;

@SuppressWarnings({"unchecked", "rawtypes"})
public class MasterDatamartSerializer {

	public static void serialize(MasterDatamart<?> datamart, Writer writer) throws IOException {
		Json.toJson(new SerializedDatamart(datamart.name(), datamart.toMap()), writer);
	}

	public static <T> MasterDatamart<T> deserialize(Reader reader) throws IOException {
		try(reader) {
			SerializedDatamart serializedDatamart = Json.fromJson(reader, SerializedDatamart.class);
			return (MasterDatamart<T>) new MapMessageMasterDatamart(serializedDatamart.name, serializedDatamart.data);
		}
	}

	private static class SerializedDatamart implements Serializable {
		private final String name;
		private final Map data;

		public SerializedDatamart(String name, Map data) {
			this.name = name;
			this.data = data;
		}
	}
}
