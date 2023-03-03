package io.intino.datahub.master.serialization;

import io.intino.alexandria.Json;
import io.intino.alexandria.message.Message;
import io.intino.alexandria.zim.ZimWriter;
import io.intino.datahub.master.MasterDatamart;
import io.intino.datahub.master.datamarts.messages.MapMessageMasterDatamart;

import javax.jms.BytesMessage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

@SuppressWarnings({"unchecked", "rawtypes"})
public class MasterDatamartSerializer {

	public static void serialize(MasterDatamart<?> datamart, OutputStream outputStream) throws IOException {
		if(datamart.elementType().equals(Message.class)) {
			serializeMessageDatamart((MasterDatamart<Message>) datamart, outputStream);
		}
		throw new IllegalArgumentException("Datamart of " + datamart.elementType() + " not supported");
	}

	private static void serializeMessageDatamart(MasterDatamart<Message> datamart, OutputStream outputStream) throws IOException {
		try(ZimWriter writer = new ZimWriter(outputStream)) {
			Iterator<Message> messages = datamart.elements().iterator();
			while(messages.hasNext()) {
				writer.write(messages.next());
			}
		}
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
