package io.intino.ness.triton.datalake;

import io.intino.alexandria.Timetag;
import io.intino.alexandria.logger.Logger;
import io.intino.ness.datalake.Datalake.EventStore.Tank;
import io.intino.ness.triton.box.ServiceBox;
import io.intino.ness.triton.box.Utils;
import io.intino.ness.triton.bus.BusManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class TankWriter {
	private final BusManager bus;
	private final Tank tank;
	private final ServiceBox box;
	private final File temporalSession;

	public TankWriter(ServiceBox box, Tank tank) {
		this.box = box;
		this.bus = box.busManager();
		this.tank = tank;
		this.temporalSession = box.temporalSession();

	}

	public void register() {
		bus.registerConsumer(tank.name(), message -> save(MessageTranslator.toInlMessage(message)));
	}

	private void save(io.intino.alexandria.inl.Message message) {
		try {
			Files.write(destination(tank, message).toPath(), (message.toString() + "\n\n").getBytes(), APPEND, CREATE);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private File destination(Tank tank, io.intino.alexandria.inl.Message message) {
		return new File(temporalSession, tank.name() + "#" + timetag(message).value() + ".inl");
	}

	private Timetag timetag(io.intino.alexandria.inl.Message message) {
		return Utils.timetag(Instant.parse(message.get("ts").data()), box.scale());
	}
}