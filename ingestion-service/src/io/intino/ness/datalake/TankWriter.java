package io.intino.ness.datalake;

import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.ness.box.NessServiceBox;
import io.intino.ness.box.Utils;
import io.intino.ness.bus.BusManager;
import io.intino.ness.datalake.Datalake.EventStore.Tank;
import io.intino.ness.core.Fingerprint;
import io.intino.ness.core.Session;

import javax.jms.Message;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.stream.Stream;

import static io.intino.ness.datalake.MessageTranslator.toInlMessage;

public class TankWriter {
	private final BusManager bus;
	private final Tank tank;
	private final Scale scale;
	private final NessServiceBox box;

	public TankWriter(NessServiceBox box, Tank tank) {
		this.box = box;
		this.bus = box.busManager();
		this.scale = box.scale();
		this.tank = tank;
	}

	public void register() {
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
		box.datalake().push(Stream.of(new TemporalSession(tank, timetag(message), message)));
	}

	private Timetag timetag(io.intino.alexandria.inl.Message message) {
		return Utils.timetag(Instant.parse(message.get("ts")), scale);
	}

	private static class TemporalSession implements Session {

		private final io.intino.alexandria.inl.Message message;
		private final Fingerprint fingerprint;

		public TemporalSession(Tank tank, Timetag timetag, io.intino.alexandria.inl.Message message) {
			this.fingerprint = Fingerprint.of(tank.name(), timetag);
			this.message = message;
		}

		@Override
		public String name() {
			return fingerprint.name();
		}

		@Override
		public Type type() {
			return Type.event;
		}

		@Override
		public InputStream inputStream() {
			return new ByteArrayInputStream(message.toString().getBytes());
		}
	}
}