package io.intino.ness.datalake;

import io.intino.alexandria.Scale;
import io.intino.ness.box.NessBox;
import io.intino.ness.box.Utils;
import io.intino.ness.bus.BusManager;
import io.intino.ness.core.Datalake.EventStore.Tank;
import io.intino.ness.core.fs.FSDatalake;
import io.intino.ness.core.memory.MemoryStage;
import io.intino.ness.core.sessions.EventSession;

import javax.jms.Message;
import java.time.Instant;

import static io.intino.ness.datalake.MessageTranslator.toInlMessage;

public class TankStarter {
	private final FSDatalake datalake;
	private final BusManager bus;
	private final Tank tank;
	private final Scale scale;

	public TankStarter(NessBox box, Tank tank) {
		this.datalake = box.datalake();
		this.bus = box.busManager();
		this.scale = box.scale();
		this.tank = tank;
	}

	public void start() {
		bus.registerConsumer(Probes.feed(tank), message -> feed(tank, message));
		bus.registerConsumer(Probes.put(tank), message -> put(tank, message));
	}

	private void feed(Tank tank, Message message) {
		new Thread(() -> flow(tank, message)).start();
		put(tank, toInlMessage(message));
	}

	private void put(Tank tank, Message message) {
		put(tank, toInlMessage(message));
	}

	@SuppressWarnings("ConstantConditions")
	private void flow(Tank tank, Message message) {
		bus.getTopicProducer(Probes.flow(tank)).produce(message);
	}

	private void put(Tank tank, io.intino.alexandria.inl.Message message) {
		send(tank, message);
	}

	private void send(Tank tank, io.intino.alexandria.inl.Message message) {
		MemoryStage stage = new MemoryStage();
		EventSession session = stage.createEventSession();
		session.put(tank.name(), Utils.timetag(Instant.parse(message.get("ts")), scale), message);
		session.close();
		datalake.push(stage.blobs());
		stage.clear();
	}
}