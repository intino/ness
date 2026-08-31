package systems.intino.eventsourcing.datahub.box.service.jms;

import com.google.gson.JsonObject;
import io.intino.alexandria.Json;
import io.intino.alexandria.logger.Logger;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import org.apache.activemq.command.ActiveMQBytesMessage;
import systems.intino.eventsourcing.datahub.box.DatahubBox;
import systems.intino.eventsourcing.datalake.Datalake;
import systems.intino.eventsourcing.datalake.file.message.MessageEventTub;
import systems.intino.eventsourcing.datalake.file.resource.ResourceEventTub;
import systems.intino.eventsourcing.event.Event;
import systems.intino.eventsourcing.jms.MessageReader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static systems.intino.eventsourcing.datahub.broker.jms.JmsMessageTranslator.toJmsMessage;

public class DatalakeRequest {

	private final DatahubBox box;

	public DatalakeRequest(DatahubBox box) {
		this.box = box;
	}

	public Stream<Message> accept(Message request) {
		try {
			return handleDatalakeDownload(MessageReader.textFrom(request).trim());
		} catch (Throwable e) {
			Logger.error(e);
			return Stream.empty();
		}
	}

	private Stream<Message> handleDatalakeDownload(String request) {
		if (request.equals("[Datalake]")) return Stream.of(toJmsMessage((box.datalake()).root().getAbsolutePath()));
		if (request.equals("messageStore/tanks")) return tankResponse(box.datalake().messageStore());
		if (request.equals("resourceStore/tanks")) return tankResponse(box.datalake().resourceStore());
		if (request.startsWith("{")) {
			JsonObject jsonObject = Json.fromString(request, JsonObject.class);
			if ("reflow".equals(jsonObject.get("operation").getAsString())) return reflow(jsonObject);
		}
		return Stream.empty();
	}

	private static <T extends Event> Stream<Message> tankResponse(Datalake.Store<T> store) {
		return Stream.of(toJmsMessage(Json.toString(store.tanks().map(DatalakeRequest::tankOf).collect(toList()))));
	}

	private Stream<Message> reflow(JsonObject request) {
		String tank = request.get("tank").getAsString();
		String source = request.has("source") ? request.get("source").getAsString() : null;
		List<String> tubs = new ArrayList<>();
		request.get("tubs").getAsJsonArray().forEach(v -> tubs.add(v.getAsString()));
		List<File> files = filesOf(tank, source, tubs).toList();
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

	private Stream<File> filesOf(String tank, String source, List<String> tubs) {
		if (box.datalake().messageStore().containsTank(tank)) return messageFilesOf(tank, source, tubs);
		if (box.datalake().resourceStore().containsTank(tank)) return resourceFilesOf(tank, source, tubs);
		return Stream.empty();
	}

	private Stream<File> messageFilesOf(String tank, String source, List<String> tubs) {
		return sourcesOf(box.datalake().messageStore().tank(tank), source)
				.flatMap(Datalake.Store.Source::tubs)
				.filter(t -> tubs.contains(t.timetag().value()))
				.map(t -> ((MessageEventTub) t).file());
	}

	private Stream<File> resourceFilesOf(String tank, String source, List<String> tubs) {
		return sourcesOf(box.datalake().resourceStore().tank(tank), source)
				.flatMap(Datalake.Store.Source::tubs)
				.filter(t -> tubs.contains(t.timetag().value()))
				.map(t -> ((ResourceEventTub) t).file());
	}

	private static <T extends Event> Stream<Datalake.Store.Source<T>> sourcesOf(Datalake.Store.Tank<T> tank, String source) {
		Stream<Datalake.Store.Source<T>> sources = tank.sources();
		return source == null || source.isEmpty() ? sources : sources.filter(s -> source.equals(s.name()));
	}

	private static <T extends Event> Tank tankOf(Datalake.Store.Tank<T> tank) {
		String scale = tank.scale() != null ? tank.scale().name() : null;
		return new Tank(tank.name(), scale, tank.sources().map(DatalakeRequest::sourceOf).collect(toList()));
	}

	private static <T extends Event> Source sourceOf(Datalake.Store.Source<T> source) {
		return new Source(source.name(), source.tubs().map(t -> t.timetag().value()).collect(toList()));
	}

	private static class Tank {
		String name;
		String scale;
		List<Source> sources;

		public Tank(String name, String scale, List<Source> sources) {
			this.name = name;
			this.scale = scale;
			this.sources = sources;
		}
	}

	private static class Source {
		String name;
		List<String> tubs;

		public Source(String name, List<String> tubs) {
			this.name = name;
			this.tubs = tubs;
		}
	}
}
