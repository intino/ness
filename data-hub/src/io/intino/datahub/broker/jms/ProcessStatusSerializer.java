package io.intino.datahub.broker.jms;

import io.intino.alexandria.Fingerprint;
import io.intino.alexandria.Scale;
import io.intino.alexandria.Session;
import io.intino.alexandria.Timetag;
import io.intino.alexandria.event.Event;
import io.intino.alexandria.event.message.MessageEvent;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.Message;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.function.Consumer;

import static io.intino.alexandria.Timetag.of;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.time.LocalDateTime.ofInstant;
import static java.time.ZoneOffset.UTC;

class ProcessStatusSerializer {
	private final File stage;
	private final String tank;
	private final Scale scale;

	ProcessStatusSerializer(File stage, String processStatusTank, Scale scale) {
		this.stage = stage;
		this.tank = processStatusTank;
		this.scale = scale;
	}

	Consumer<javax.jms.Message> create() {
		return message -> save(MessageTranslator.toInlMessages(message));
	}

	private void save(Iterator<Message> messages) {
		messages.forEachRemaining(m -> write(destination(m).toPath(), m));
	}

	private void write(Path path, Message message) {
		try {
			Files.writeString(path, message.toString() + "\n\n", CREATE, APPEND);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private File destination(Message message) {
		MessageEvent event = new MessageEvent(message);
		String fingerprint = Fingerprint.of(tank, event.ss(), timetag(event), Event.Format.Message).name();
		return new File(stage, fingerprint + Session.SessionExtension);
	}

	private Timetag timetag(MessageEvent event) {
		return of(ofInstant(event.ts(), UTC), scale);
	}
}