package io.intino.datahub.broker.jms;

import io.intino.alexandria.*;
import io.intino.alexandria.event.Event.Format;
import io.intino.alexandria.event.EventWriter;
import io.intino.alexandria.event.message.MessageEvent;
import io.intino.alexandria.event.resource.ResourceEvent;
import io.intino.alexandria.event.resource.ResourceEventWriter;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.Message;
import io.intino.datahub.datamart.MasterDatamart;
import io.intino.datahub.datamart.MasterDatamartRepository;
import io.intino.datahub.datamart.messages.MasterDatamartMessageMounter;
import io.intino.datahub.model.Datalake;

import javax.jms.BytesMessage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

import static io.intino.alexandria.Timetag.of;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.time.ZoneOffset.UTC;

@SuppressWarnings("unchecked")
class JmsMessageSerializer {
	private final File stage;
	private final Datalake.Tank tank;
	private final Scale scale;
	private final MasterDatamartMessageMounter[] mounters;

	JmsMessageSerializer(File stage, Datalake.Tank tank, Scale scale, MasterDatamartRepository datamarts) {
		this.stage = stage;
		this.tank = tank;
		this.scale = scale;
		this.mounters = createMountersFor(tank, datamarts);
	}

	Consumer<javax.jms.Message> create() {
		if(tank.isMessage()) return new MessageHandler();
		if(tank.isMeasurement()) return new MeasurementHandler();
		if(tank.isResource()) return new ResourceHandler();
		return Handler.empty();
	}

	private Timetag timetag(Instant ts) {
		return Timetag.of(LocalDateTime.ofInstant(ts, UTC), scale);
	}

	private static MasterDatamartMessageMounter[] createMountersFor(Datalake.Tank tank, MasterDatamartRepository datamartsRepo) {
		if (!tank.isMessage() || tank.asMessage() == null || tank.asMessage().message() == null)
			return new MasterDatamartMessageMounter[0];
		return datamartsRepo.datamarts().stream()
				.filter(d -> d.elementType().equals(Message.class))
				.filter(d -> d.subscribedEvents().contains(tank.asMessage().message().name$()))
				.map(d -> new MasterDatamartMessageMounter((MasterDatamart<Message>) d))
				.toArray(MasterDatamartMessageMounter[]::new);
	}

	private interface Handler extends Consumer<javax.jms.Message> {
		static Handler empty() {
			return m -> {};
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
				if (tank.isMessage()) mount(message);
			}
		}

		private void mount(Message message) {
			try {
				for (MasterDatamartMessageMounter mounter : mounters) mounter.mount(message);
			} catch (Exception e) {
				Logger.error("Error while mounting message of tank " + tank.name$() + ": " + e.getMessage(), e);
			}
		}

		private void save(Message message) {
			write(destination(message).toPath(), message);
		}

		protected File destination(Message message) {
			MessageEvent event = new MessageEvent(message);
			String fingerprint = Fingerprint.of(tank.qn(), event.ss(), timetag(event.ts()), Format.Message).name();
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
			String fingerprint = Fingerprint.of(tank.qn(), event.ss(), timetag(event.ts()), Format.Measurement).name();
			return new File(stage, fingerprint + Session.SessionExtension);
		}
	}

	private class ResourceHandler implements Handler {

		@Override
		public void accept(javax.jms.Message message) {
			try {
				ResourceEvent event = readResourceEventFrom((BytesMessage) message);
				String fingerprint = Fingerprint.of(tank.qn(), event.ss(), timetag(event.ts()), Format.Resource).name();
				File destination = new File(stage, fingerprint + Session.SessionExtension);
				appendToDestinationFile(event, destination);
			} catch (Exception e) {
				Logger.error(e);
			}
		}

		private void appendToDestinationFile(ResourceEvent event, File destination) throws IOException {
			try(EventWriter<ResourceEvent> writer = new ResourceEventWriter(destination, true)) {
				writer.write(event);
			}
		}

		private ResourceEvent readResourceEventFrom(BytesMessage m) throws Exception {
			String resourceName = m.getStringProperty("resource.name");
			Map<String, String> metadata = Json.fromJson(m.getStringProperty("resource.metadata"), Map.class);

			int dataLength = m.getIntProperty("resource.data.length");
			byte[] data = new byte[dataLength];
			m.readBytes(data);

			Resource resource = new Resource(resourceName, data);
			resource.metadata().putAll(metadata);

			String type = m.getStringProperty("type");
			String ss = m.getStringProperty("ss");
			Instant ts = Instant.ofEpochMilli(m.getLongProperty("ts"));
			return new ResourceEvent(type, ss, resource).ts(ts);
		}
	}
}