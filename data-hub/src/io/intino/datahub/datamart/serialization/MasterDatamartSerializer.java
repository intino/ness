package io.intino.datahub.datamart.serialization;

import io.intino.alexandria.message.Message;
import io.intino.alexandria.zim.ZimStream;
import io.intino.alexandria.zim.ZimWriter;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.datamart.MasterDatamart;
import io.intino.datahub.datamart.messages.MapMessageMasterDatamart;
import io.intino.datahub.model.Data;
import io.intino.datahub.model.Datamart;

import java.io.*;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

@SuppressWarnings({"unchecked", "rawtypes"})
public class MasterDatamartSerializer {

	public static void serialize(MasterDatamart<?> datamart, File file) throws IOException {
		serialize(datamart, new FileOutputStream(file));
	}

	public static void serialize(MasterDatamart<?> datamart, OutputStream outputStream) throws IOException {
		if(datamart.elementType().equals(Message.class)) {
			serializeMessageDatamart((MasterDatamart<Message>) datamart, outputStream);
		} else {
			throw new IllegalArgumentException("Datamart of " + datamart.elementType() + " not supported");
		}
	}

	private static void serializeMessageDatamart(MasterDatamart<Message> datamart, OutputStream outputStream) throws IOException {
		try(ZimWriter writer = new ZimWriter(outputStream)) {
			Iterator<Message> messages = datamart.elements().iterator();
			while(messages.hasNext()) {
				writer.write(messages.next());
			}
		}
	}

	public static <T> MasterDatamart<T> deserialize(File file, Datamart definition) throws IOException {
		return deserialize(new FileInputStream(file), definition);
	}

	public static <T> MasterDatamart<T> deserialize(InputStream inputStream, Datamart definition) throws IOException {
		try(Stream<Message> messages = ZimStream.of(inputStream)) {
			return (MasterDatamart<T>) new MapMessageMasterDatamart(definition, messages);
		}
	}

	public static File backupFileOf(Datamart datamart, DataHubBox box) {
		File file = new File(box.configuration().backupDirectory(), "datamarts/" + datamart.name$() + ".backup");
		file.getParentFile().mkdirs();
		return file;
	}
}
