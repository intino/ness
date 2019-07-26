package io.intino.ness.datahub.broker.jms;

import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.alexandria.datalake.Datalake.EventStore.Tank;
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

public class TankManager {
	private final BrokerManager bus;
	private final File temporalSession;
	private final Tank tank;
	private final Scale scale;

	public TankManager(BrokerManager bus, File temporalSession, Tank tank, Scale scale) {
		this.bus = bus;
		this.temporalSession = temporalSession;
		this.tank = tank;
		this.scale = scale;
	}

	public void register() {
		bus.registerConsumer(tank.name(), message -> save(MessageTranslator.toInlMessage(message)));
	}

	private void save(Message message) {
		try {
			Files.write(destination(tank, message).toPath(), (message.toString() + "\n\n").getBytes(), APPEND, CREATE);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private File destination(Tank tank, Message message) {
		return new File(temporalSession, tank.name() + "#" + timetag(message).value() + ".inl");
	}

	private Timetag timetag(Message message) {
		return of(ofInstant(Instant.parse(message.get("ts").data()), UTC), scale);
	}
}