package io.intino.datahub.box.service.jms;

import com.google.gson.JsonObject;
import io.intino.alexandria.Json;
import io.intino.alexandria.datalake.Datalake;
import io.intino.alexandria.datalake.file.message.MessageEventTub;
import io.intino.alexandria.event.message.MessageEvent;
import io.intino.alexandria.jms.MessageReader;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.box.DataHubBox;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import org.apache.activemq.command.ActiveMQBytesMessage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static io.intino.datahub.broker.jms.JmsMessageTranslator.toJmsMessage;
import static java.util.stream.Collectors.toList;

public class DatalakeRequest {

	private final DataHubBox box;

	public DatalakeRequest(DataHubBox box) {
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
		if (request.equals("messageStore/tanks"))
			return Stream.of(toJmsMessage(Json.toString(box.datalake().messageStore().tanks()
					.map(DatalakeRequest::tankOf).collect(toList()))));
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