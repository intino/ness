	package io.intino.datahub.broker.jms;

import io.intino.alexandria.Fingerprint;
import io.intino.alexandria.Scale;
import io.intino.alexandria.Session;
import io.intino.alexandria.Timetag;
import io.intino.alexandria.event.Event.Format;
import io.intino.alexandria.event.message.MessageEvent;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.Message;
import io.intino.datahub.datamart.MasterDatamart;
import io.intino.datahub.datamart.MasterDatamartRepository;
import io.intino.datahub.datamart.messages.MasterDatamartMessageMounter;
import io.intino.datahub.model.Datalake;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.function.Consumer;

import static io.intino.alexandria.Timetag.of;
import static io.intino.alexandria.event.Event.Format.Measurement;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.time.LocalDateTime.ofInstant;
import static java.time.ZoneOffset.UTC;

@SuppressWarnings("unchecked")
class MessageSerializer {
	private final File stage;
	private final Datalake.Tank tank;
	private final Scale scale;
	private final MasterDatamartMessageMounter[] mounters;

	MessageSerializer(File stage, Datalake.Tank tank, Scale scale, MasterDatamartRepository datamarts) {
		this.stage = stage;
		this.tank = tank;
		this.scale = scale;
		this.mounters = createMountersFor(tank, datamarts);
	}

	Consumer<javax.jms.Message> create() {
		return message -> consume(MessageTranslator.toInlMessages(message));
	}

	private void consume(Iterator<Message> messages) {
		while(messages.hasNext()) {
			Message message = messages.next();
			save(message);
			mount(message);
		}
	}

	private void mount(Message message) {
		for(MasterDatamartMessageMounter mounter : mounters) {
			mounter.mount(message);
		}
	}

	private void save(Message message) {
		write(destination(message).toPath(), message);
	}

	private File destination(Message message) {
		MessageEvent event = new MessageEvent(message);
		String fingerprint = Fingerprint.of(tank.qn(), event.ss(), timetag(event), tank.isMessage() ? Format.Message : Measurement).name();
		return new File(stage, fingerprint + Session.SessionExtension);
	}

	private void write(Path path, Message message) {
		try {
			Files.writeString(path, message.toString() + "\n\n", CREATE, APPEND);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private Timetag timetag(MessageEvent event) {
		return of(ofInstant(event.ts(), UTC), scale);
	}

	private static MasterDatamartMessageMounter[] createMountersFor(Datalake.Tank tank, MasterDatamartRepository datamartsRepo) {
		if(!tank.isMessage() || tank.asMessage() == null || tank.asMessage().message() == null) return new MasterDatamartMessageMounter[0];
		return datamartsRepo.datamarts().stream()
				.filter(d -> d.elementType().equals(Message.class))
				.filter(d -> d.subscribedEvents().contains(tank.asMessage().message().name$()))
				.map(d -> new MasterDatamartMessageMounter((MasterDatamart<Message>) d))
				.toArray(MasterDatamartMessageMounter[]::new);
	}
}