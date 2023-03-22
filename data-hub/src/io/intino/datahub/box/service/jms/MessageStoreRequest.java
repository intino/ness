package io.intino.datahub.box.service.jms;

import com.google.gson.JsonObject;
import io.intino.alexandria.Json;
import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.alexandria.datalake.Datalake;
import io.intino.alexandria.datalake.file.message.MessageEventTub;
import io.intino.alexandria.event.message.MessageEvent;
import io.intino.alexandria.jms.MessageReader;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.datamart.MasterDatamart;
import io.intino.datahub.datamart.serialization.MasterDatamartSerializer;
import io.intino.datahub.datamart.serialization.MasterDatamartSnapshots;
import org.apache.activemq.command.ActiveMQBytesMessage;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.JMSException;
import javax.jms.Message;
import java.io.*;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static io.intino.datahub.broker.jms.MessageTranslator.toJmsMessage;
import static io.intino.datahub.datamart.serialization.MasterDatamartSnapshots.loadMostRecentSnapshotTo;
import static java.util.stream.Collectors.toList;

public class MessageStoreRequest {

	private final DataHubBox box;

	public MessageStoreRequest(DataHubBox box) {
		this.box = box;
	}

	public Stream<Message> accept(Message request) {
		try {
			String content = MessageReader.textFrom(request);
			return content.startsWith("datamart") ? handleDatamartDownload(content) : handleDatalakeDownload(content);
		} catch (Throwable e) {
			Logger.error(e);
			return Stream.empty();
		}
	}

	private Stream<Message> handleDatamartDownload(String request) {
		String[] command = request.split(":", 3);
		if(command.length < 3) return fail("Datamart requests must be like this: datamart:<name>:[snapshots | timetag], but it was " + request);

		String datamartName = command[1].trim();
		String operation = command[2].trim();

		return operation.equals("snapshots")
				? listAvailableSnapshotsOf(datamartName)
				: downloadDatamart(datamartName, operation);
	}

	private Stream<Message> downloadDatamart(String datamartName, String timetag) {
		if(timetag.isEmpty()) {
			MasterDatamart<?> datamart = box.datamarts().get(datamartName);
			return datamart == null ? Stream.empty() : downloadDatamart(datamart);
		}
		return loadMostRecentSnapshotTo(box.datamarts().root(), datamartName, asTimetag(timetag), box.graph())
				.map(MasterDatamart.Snapshot::datamart)
				.map(this::downloadDatamart)
				.orElse(Stream.empty());
	}

	private Timetag asTimetag(String timetag) {
		return timetag.isEmpty() ? Timetag.of(LocalDate.now(), Scale.Day) : Timetag.of(timetag);
	}

	private Stream<Message> listAvailableSnapshotsOf(String datamart) {
		List<Timetag> snapshots = MasterDatamartSnapshots.listAvailableSnapshotsOf(box.datamarts().root(), datamart);
		if(snapshots.isEmpty()) return Stream.empty();
		try {
			ActiveMQTextMessage message = new ActiveMQTextMessage();
			message.setIntProperty("size", snapshots.size());
			message.setText(snapshots.stream().map(Timetag::value).collect(Collectors.joining(",")));
			return Stream.of(message);
		} catch (Exception e) {
			Logger.error(e);
			return Stream.empty();
		}
	}

	private Stream<Message> downloadDatamart(MasterDatamart<?> datamart) {
		try {
			ActiveMQBytesMessage message = new ActiveMQBytesMessage();
			message.setIntProperty("size", datamart.size());
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream(8192);
			MasterDatamartSerializer.serialize(datamart, outputStream);
			byte[] bytes = outputStream.toByteArray();
			message.writeBytes(bytes);
			message.setIntProperty("content-size", bytes.length);
			return Stream.of(message);
		} catch (Exception e) {
			Logger.error(e);
			return Stream.empty();
		}
	}

	private Stream<Message> handleDatalakeDownload(String request) {
		if (request.equals("datalake")) return Stream.of(toJmsMessage((box.datalake()).root().getAbsolutePath()));
		if (request.equals("eventStore/tanks"))
			return Stream.of(toJmsMessage(Json.toString(box.datalake().messageStore().tanks()
					.map(MessageStoreRequest::tankOf).collect(toList()))));
		if (request.startsWith("{")) {
			JsonObject jsonObject = Json.fromString(request, JsonObject.class);
			if ("reflow".equals(jsonObject.get("operation").getAsString())) return reflow(jsonObject);
		}
		return Stream.empty();
	}

	private Stream<Message> reflow(JsonObject request) {
		String tank = request.get("tank").getAsString();
		List<String> tubs = new ArrayList<>();
		request.get("tubs").getAsJsonArray().forEach(v -> tubs.add(v.getAsString()));
		List<File> files = filesOf(tank, tubs).toList();
		return IntStream.range(0, files.size())
				.mapToObj(i -> toMessage(read(files.get(i)), i < files.size() - 1))
				.filter(Objects::nonNull);
	}

	private static Message toMessage(byte[] content, boolean hasNext) {
		try {
			ActiveMQBytesMessage message = new ActiveMQBytesMessage();
			message.setBooleanProperty("hasNext", hasNext);
			message.writeBytes(content);
			return message;
		} catch (JMSException e) {
			Logger.error(e);
			return null;
		}
	}

	private static byte[] read(File f) {
		try {
			return Files.readAllBytes(f.toPath());
		} catch (IOException e) {
			Logger.error(e);
			return new byte[0];
		}
	}

	private Stream<File> filesOf(String tank, List<String> tubs) {
		return box.datalake().messageStore().tank(tank).sources().flatMap(Datalake.Store.Source::tubs).filter(t -> tubs.contains(t.timetag().value())).map(t -> ((MessageEventTub) t).file());
	}

	private static Tank tankOf(Datalake.Store.Tank<MessageEvent> t) {
		return new Tank(t.name(), t.scale().name(), t.sources().map(Datalake.Store.Source::name).collect(toList()));
	}

	private static <T> Stream<T> fail(String msg) {
		Logger.error(msg);
		return Stream.empty();
	}

	private static class Tank {
		String name;
		String scale;
		List<String> sources;

		public Tank(String name, String scale, List<String> sources) {
			this.name = name;
			this.scale = scale;
			this.sources = sources;
		}
	}
}