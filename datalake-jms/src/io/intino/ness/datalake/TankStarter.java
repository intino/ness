package io.intino.ness.datalake;

import io.intino.ness.bus.BusManager;
import io.intino.ness.datalake.graph.Tank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Message;

import static io.intino.konos.jms.Consumer.textFrom;
import static io.intino.ness.datalake.MessageTranslator.toInlMessage;

public class TankStarter {
	private static final Logger logger = LoggerFactory.getLogger(TankStarter.class);

	private BusManager bus;
	private Tank tank;

	public TankStarter(BusManager manager, Tank tank) {
		this.bus = manager;
		this.tank = tank;
	}

	public void start() {
		bus.registerConsumer(tank.feedQN(), message -> feed(tank, message));
		bus.registerConsumer(tank.dropQN(), message -> drop(tank, message));
	}

	private void feed(Tank tank, Message message) {
		new Thread(() -> flow(tank, message)).start();
		drop(tank, toInlMessage(message));
	}

	private void drop(Tank tank, Message message) {
		drop(tank, toInlMessage(message));
	}

	@SuppressWarnings("ConstantConditions")
	private void flow(Tank tank, Message message) {
		bus.getTopicProducer(tank.flowQN()).produce(message);
	}

	private void drop(Tank tank, io.intino.ness.inl.Message message) {
		tank.drop(message);
	}
}