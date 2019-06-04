package io.intino.ness.triton.datalake;

import io.intino.alexandria.Timetag;
import io.intino.alexandria.logger.Logger;
import io.intino.ness.datalake.Datalake.EventStore.Tank;
import io.intino.ness.triton.box.TritonBox;
import io.intino.ness.triton.box.Utils;
import io.intino.ness.triton.bus.BusManager;

import javax.jms.Message;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

public class TankWriter {
	private final BusManager bus;
	private final Tank tank;
	private final TritonBox box;
	private final File temporalSession;

	public TankWriter(TritonBox box, Tank tank) {
		this.box = box;
		this.bus = box.busManager();
		this.tank = tank;
		this.temporalSession = box.temporalSession();

	}

	public void register() {
		bus.registerConsumer(Probes.feed(tank), message -> feed(tank, message));
		bus.registerConsumer(Probes.put(tank), message -> put(tank, message));
	}

	private void feed(Tank tank, Message message) {
		new Thread(() -> flow(tank, message)).start();
		put(tank, MessageTranslator.toInlMessage(message));
	}

	private void put(Tank tank, Message message) {
		put(tank, MessageTranslator.toInlMessage(message));
	}

	@SuppressWarnings("ConstantConditions")
	private void flow(Tank tank, Message message) {
		bus.getTopicProducer(Probes.flow(tank)).produce(message);
	}

	private void put(Tank tank, io.intino.alexandria.inl.Message message) {
		save(tank, message);
	}

	private void save(Tank tank, io.intino.alexandria.inl.Message message) {
		try {
			Files.write(destination(tank, message).toPath(), (message.toString() + "\n\n").getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
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