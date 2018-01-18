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

	public TankStarter(BusManager manager, DatalakeManager datalakeManager) {
		this.bus = manager;
		this.datalakeManager = datalakeManager;
	}

	public void start(Tank tank) {
		bus.registerConsumer(tank.feedQN(), message -> consume(tank, message));
	}

	private void consume(Tank tank, javax.jms.Message message) {
		String text = textFrom(message);
		if (text.contains("created:")) text = text.replace("created:", "ts:");
		consume(tank, load(text));
	}

	private void consume(io.intino.ness.graph.Tank aTank, Message message) {
		try {
			datalakeManager.drop(aTank, message);
		} catch (Throwable e) {
			logger.error("error processing message: " + message.toString());
			logger.error(e.getMessage(), e);
		}
	}
}