package io.intino.ness.datalake;

import io.intino.alexandria.Scale;
import io.intino.ness.box.NessBox;
import io.intino.ness.box.Utils;
import io.intino.ness.bus.BusManager;
import io.intino.ness.core.Datalake.EventStore.Tank;
import io.intino.ness.core.EventSession;

import javax.jms.Message;
import java.io.File;
import java.time.Instant;

import static io.intino.ness.datalake.MessageTranslator.toInlMessage;

public class TankWriter {
	private final BusManager bus;
	private final Tank tank;
	private final Scale scale;
	private final File stage;
	private final NessBox box;

	public TankWriter(NessBox box, Tank tank) {
		this.box = box;
		this.bus = box.busManager();
		this.scale = box.scale();
		this.tank = tank;
		this.stage = new File(box.datalakeStageDirectory());
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
		save(tank, message);
	}

	private void save(Tank tank, io.intino.alexandria.inl.Message message) {
		EventSession session = box.stage().createEventSession();//TODO change if is remote
		session.put(tank.name(), Utils.timetag(Instant.parse(message.get("ts")), scale), message);
		session.close();
	}
}