package io.intino.ness.datalake;

import io.intino.ness.bus.BusManager;
import io.intino.ness.graph.Tank;
import io.intino.ness.inl.Message;

import static io.intino.konos.jms.Consumer.logger;
import static io.intino.konos.jms.Consumer.textFrom;
import static io.intino.ness.inl.Message.load;

public class TankStarter {

	private BusManager bus;
	private DatalakeManager datalake;

	public TankStarter(BusManager manager, DatalakeManager datalake) {
		this.bus = manager;
		this.datalake = datalake;
	}

	public void start(Tank tank) {
		bus.registerConsumer(tank.feedQN(), message -> start(tank, message));
	}

	private void start(Tank tank, javax.jms.Message message) {
		start(tank, load(textFrom(message)));
	}

	private void start(io.intino.ness.graph.Tank aTank, Message message) {
		try {
			datalake.station().drop(aTank.qualifiedName()).register(message);
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
	}
}
