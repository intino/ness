package io.intino.ness.datalake;

import io.intino.ness.bus.BusManager;
import io.intino.ness.graph.Tank;
import io.intino.ness.inl.Message;

import static io.intino.konos.jms.Consumer.logger;
import static io.intino.konos.jms.Consumer.textFrom;
import static io.intino.ness.inl.Message.load;

public class TankStarter {

	private BusManager bus;
	private DatalakeManager datalakeManager;
	private Tank tank;

	public TankStarter(BusManager manager, DatalakeManager datalakeManager, Tank tank) {
		this.bus = manager;
		this.datalakeManager = datalakeManager;
		this.tank = tank;
	}

	public void start() {
		bus.registerConsumer(tank.feedQN(), message -> consume(tank, message));
	}

	private void consume(Tank tank, javax.jms.Message message) {
		new Thread(() -> flow(tank, message)).start();
		consume(tank, textFrom(message));
	}

	@SuppressWarnings("ConstantConditions")
	private void flow(Tank tank, javax.jms.Message message) {
		bus.getProducer(tank.flowQN()).produce(message);
	}

	private void consume(Tank aTank, String textMessage) {
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