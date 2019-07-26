package io.intino.ness.datahub.broker.jms;

import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.alexandria.jms.Consumer;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.Message;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;

import static io.intino.alexandria.Timetag.of;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.time.LocalDateTime.ofInstant;
import static java.time.ZoneOffset.UTC;

class TopicSaver {
	private final File stage;
	private final String tank;
	private final Scale scale;

	TopicSaver(File stage, String tank, Scale scale) {
		this.stage = stage;
		this.tank = tank;
		this.scale = scale;
	}

	public Consumer create() {
		return message -> save(MessageTranslator.toInlMessage(message));
	}

	private void save(Message message) {
		try {
			Files.write(destination(tank, message).toPath(), (message.toString() + "\n\n").getBytes(), APPEND, CREATE);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private File destination(String tank, Message message) {
		return new File(stage, tank + "#" + timetag(message).value() + ".inl");
	}

	private Timetag timetag(Message message) {
		return of(ofInstant(Instant.parse(message.get("ts").data()), UTC), scale);
	}
}