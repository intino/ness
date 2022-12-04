package io.intino.datahub.box.service.jms;

import com.google.gson.JsonObject;
import io.intino.alexandria.Json;
import io.intino.alexandria.datalake.Datalake;
import io.intino.alexandria.datalake.file.FileEventTub;
import io.intino.alexandria.jms.MessageReader;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.broker.BrokerManager;
import org.apache.activemq.command.ActiveMQBytesMessage;

import javax.jms.JMSException;
import javax.jms.Message;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static io.intino.datahub.broker.jms.MessageTranslator.toJmsMessage;
import static java.util.stream.Collectors.toList;

public class DatalakeRequest {
	private final DataHubBox box;
	private final BrokerManager manager;

	public DatalakeRequest(DataHubBox box) {
		this.box = box;
		manager = box.brokerService().manager();
	}

	public Stream<Message> accept(Message request) {
		String content = MessageReader.textFrom(request);
		if (content.equals("datalake")) return Stream.of(toJmsMessage(box.datalake().root().getAbsolutePath()));
		if (content.equals("eventStore/tanks"))
			return Stream.of(toJmsMessage(Json.toString(box.datalake().eventStore().tanks()
					.map(DatalakeRequest::tankOf).collect(toList()))));
		if (content.startsWith("{")) {
			JsonObject jsonObject = Json.fromString(content, JsonObject.class);
			if ("reflow".equals(jsonObject.get("operation").getAsString())) return reflow(jsonObject);
		}
		return null;
	}

	private Stream<Message> reflow(JsonObject request) {
		String tank = request.get("tank").getAsString();
		List<String> tubs = new ArrayList<>();
		request.get("tubs").getAsJsonArray().forEach(v -> tubs.add(v.getAsString()));
		List<File> files = filesOf(tank, tubs).collect(toList());
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
		return box.datalake().eventStore().tank(tank).tubs().filter(t -> tubs.contains(t.timetag().value())).map(t -> ((FileEventTub) t).file());
	}

	private static Tank tankOf(Datalake.EventStore.Tank t) {
		return new Tank(t.name(), t.scale().name(), t.tubs().map(tb -> tb.timetag().value()).collect(toList()));
	}

	private static class Tank {
		String name;
		String scale;
		List<String> tubs;

		public Tank(String name, String scale, List<String> tubs) {
			this.name = name;
			this.scale = scale;
			this.tubs = tubs;
		}
	}

	private static class InputStreamEnumeration implements Enumeration<InputStream> {
		private final Enumeration<File> files;
		private InputStream nextElement;

		InputStreamEnumeration(Collection<File> files) {
			this.files = Collections.enumeration(files);
		}

		public InputStream nextElement() {
			if (hasMore()) {
				InputStream res = nextElement;
				nextElement = null;
				return res;
			} else throw new NoSuchElementException();
		}

		public boolean hasMore() {
			if (nextElement != null) return true;
			nextElement = getNextElement();
			return (nextElement != null);
		}

		public boolean hasMoreElements() {
			return hasMore();
		}

		private InputStream getNextElement() {
			PrivilegedAction<InputStream> act = () -> {
				while (files.hasMoreElements()) try {
					return new FileInputStream(files.nextElement());
				} catch (IOException ignored) {
				}
				return null;
			};
			return AccessController.doPrivileged(act);
		}

		public void close() {
		}
	}
}