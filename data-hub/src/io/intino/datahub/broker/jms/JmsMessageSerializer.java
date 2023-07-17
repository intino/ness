package io.intino.datahub.broker.jms;

import com.google.gson.reflect.TypeToken;
import io.intino.alexandria.*;
import io.intino.alexandria.event.Event.Format;
import io.intino.alexandria.event.EventWriter;
import io.intino.alexandria.event.message.MessageEvent;
import io.intino.alexandria.event.resource.ResourceEvent;
import io.intino.alexandria.event.resource.ResourceEventWriter;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.Message;
import io.intino.datahub.datamart.MasterDatamartRepository;
import io.intino.datahub.datamart.mounters.MasterDatamartMounter;
import io.intino.datahub.model.Datalake;

import javax.jms.BytesMessage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.time.ZoneOffset.UTC;
import static java.util.stream.Collectors.toMap;

@SuppressWarnings("unchecked")
public class JmsMessageSerializer {
	private final File stage;
	private final Datalake.Tank tank;
	private final Scale scale;
	private final MasterDatamartMounter[] mounters;

	JmsMessageSerializer(File stage, Datalake.Tank tank, Scale scale, MasterDatamartRepository datamarts) {
		this.stage = stage;
		this.tank = tank;
		this.scale = scale;
		this.mounters = createMountersFor(tank, datamarts);
	}

	Consumer<javax.jms.Message> create() {
		if (tank.isMessage()) return new MessageHandler();
		if (tank.isMeasurement()) return new MeasurementHandler();
		if (tank.isResource()) return new ResourceHandler();
		return Handler.empty();
	}

	private Timetag timetag(Instant ts) {
		return Timetag.of(LocalDateTime.ofInstant(ts, UTC), scale);
	}

	private static MasterDatamartMounter[] createMountersFor(Datalake.Tank tank, MasterDatamartRepository datamartsRepo) {
		return datamartsRepo == null ? new MasterDatamartMounter[0]
				: datamartsRepo.datamarts().stream()
				.flatMap(datamart -> datamart.createMountersFor(tank))
				.toArray(MasterDatamartMounter[]::new);
	}

	private interface Handler extends Consumer<javax.jms.Message> {
		static Handler empty() {
			return m -> {
			};
		}
	}

	private class MessageHandler implements Handler {

		@Override
		public void accept(javax.jms.Message message) {
			consume(JmsMessageTranslator.toInlMessages(message));
		}

		private void consume(Iterator<Message> messages) {
			while (messages.hasNext()) {
				Message message = messages.next();
				save(message);
				mount(message);
			}
		}

		private void mount(Message message) {
			try {
				for (MasterDatamartMounter mounter : mounters) mounter.mount(message);
			} catch (Exception e) {
				Logger.error("Error while mounting message of tank " + tank.name$() + ": " + e.getMessage(), e);
			}
		}

		private void save(Message message) {
			write(destination(message).toPath(), message);
		}

		protected File destination(Message message) {
			MessageEvent event = new MessageEvent(message);
			String fingerprint = Fingerprint.of(tank.qn(), withOutParameters(event.ss()), timetag(event.ts()), Format.Message).name();
			return new File(stage, fingerprint + Session.SessionExtension);
		}

		private void write(Path path, Message message) {
			try {
				Files.writeString(path, message.toString() + "\n\n", CREATE, APPEND);
			} catch (IOException e) {
				Logger.error(e);
			}
		}

	}

	private class MeasurementHandler extends MessageHandler {

		@Override
		protected File destination(Message message) {
			MessageEvent event = new MessageEvent(message);
			String fingerprint = Fingerprint.of(tank.qn(), sensorParameter(event.ss()), timetag(event.ts()), Format.Measurement).name();
			return new File(stage, fingerprint + Session.SessionExtension);
		}

		private static String sensorParameter(String ss) {
			if (ss.contains("?")) {
				try {
					Map<String, String> map = Arrays.stream(ss.substring(ss.indexOf("?") + 1).split(";"))
							.map(p -> p.split("="))
							.collect(toMap(f -> f[0], f -> f[1]));
					if (map.containsKey("sensor")) return map.get("sensor");
				} catch (Exception e) {
					Logger.error(e);
				}
			}
			return withOutParameters(ss);
		}
	}

	private class ResourceHandler implements Handler {
		@Override
		public void accept(javax.jms.Message message) {
			try {
				ResourceEvent event = readResourceEventFrom((BytesMessage) message);
				String fingerprint = Fingerprint.of(tank.qn(), withOutParameters(event.ss()), timetag(event.ts()), Format.Resource).name();
				File destination = new File(stage, fingerprint + Session.SessionExtension);
				appendToDestinationFile(event, destination);
			} catch (Exception e) {
				Logger.error(e);
			}
		}

		private void appendToDestinationFile(ResourceEvent event, File destination) throws IOException {
			try (EventWriter<ResourceEvent> writer = new ResourceEventWriter(destination, true)) {
				writer.write(event);
			}
		}

		private ResourceEvent readResourceEventFrom(BytesMessage m) throws Exception {
			String resourceName = m.getStringProperty("resource.name");
			Resource.Metadata metadata = Json.fromJson(m.getStringProperty("resource.metadata"), Resource.Metadata.class);
			int dataLength = m.getIntProperty("resource.data.length");
			byte[] data = new byte[dataLength];
			m.readBytes(data);
			Resource resource = new Resource(resourceName, data);
			resource.metadata().putAll(metadata.properties());
			String type = m.getStringProperty("type");
			String ss = m.getStringProperty("ss");
			Instant ts = Instant.ofEpochMilli(m.getLongProperty("ts"));
			return new ResourceEvent(type, ss, resource).ts(ts);
		}
	}

	public static final Type asMap = new TypeToken<Map<String, String>>() {
	}.getType();

	private static String withOutParameters(String ss) {
		return ss.contains("?") ? ss.substring(0, ss.indexOf("?")) : ss;
	}

}