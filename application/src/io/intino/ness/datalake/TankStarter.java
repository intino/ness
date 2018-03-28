package io.intino.ness.datalake;

import io.intino.ness.bus.BusManager;
import io.intino.ness.graph.Tank;
import io.intino.ness.inl.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.intino.konos.jms.Consumer.textFrom;
import static io.intino.ness.inl.Message.load;

public class TankStarter {
	private static final Logger logger = LoggerFactory.getLogger(TankStarter.class);

	private BusManager bus;
	private DatalakeManager datalakeManager;
	private Tank tank;

	public TankStarter(BusManager manager, DatalakeManager datalakeManager, Tank tank) {
		this.bus = manager;
		this.datalakeManager = datalakeManager;
		this.tank = tank;
	}

	public void start() {
		bus.registerConsumer(tank.feedQN(), message -> feed(tank, message));
		bus.registerConsumer(tank.dropQN(), message -> drop(tank, message));
	}

	private void feed(Tank tank, javax.jms.Message message) {
		new Thread(() -> flow(tank, message)).start();
		drop(tank, textFrom(message));
	}

	private void drop(Tank tank, javax.jms.Message message) {
		drop(tank, textFrom(message));
	}

	@SuppressWarnings("ConstantConditions")
	private void flow(Tank tank, javax.jms.Message message) {
		bus.getProducer(tank.flowQN()).produce(message);
	}

	private void drop(Tank aTank, String textMessage) {
		Message message;
		try {
			message = load(textMessage);
		} catch (Throwable e) {
			logger.error("error processing message: " + textMessage);
			logger.error(e.getMessage(), e);
			return;
		}
		datalakeManager.drop(aTank, message, textMessage);
	}
}