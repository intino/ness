package io.intino.ness.datalake;

import io.intino.konos.jms.TopicProducer;
import io.intino.ness.bus.BusManager;
import io.intino.ness.graph.Tank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Session;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static io.intino.konos.jms.MessageFactory.createMessageFor;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;

class ReflowProcess {
	private final DatalakeManager datalakeManager;
	private static Logger logger = LoggerFactory.getLogger(ROOT_LOGGER_NAME);
	private BusManager bus;
	private final NessStation station;

	ReflowProcess(DatalakeManager datalakeManager, BusManager bus, NessStation station) {
		this.datalakeManager = datalakeManager;
		this.bus = bus;
		this.station = station;
	}

	void start(List<Tank> tanks) {
		if (tanks.isEmpty()) return;
		Session session = bus.transactedSession();
		if (session == null) {
			logger.error("Impossible to create transacted session");
			return;
		}
		doReflow(tanks, session);
	}

	private void doReflow(List<Tank> tanks, Session session) {
		List<TankReflowManager> reflowManagers = reflowManagers(tanks, session);
		while (flowsAreActive(reflowManagers)) beforeFlow(reflowManagers).send();
		terminateReflow(session, reflowManagers);
	}

	private void terminateReflow(Session session, List<TankReflowManager> reflowManagers) {
		try {
			session.commit();
			session.close();
			for (TankReflowManager reflowManager : reflowManagers) datalakeManager.feed(reflowManager.tank);
		} catch (JMSException e) {
			ReflowProcess.logger.error(e.getMessage(), e);
		}
	}

	private TankReflowManager beforeFlow(List<TankReflowManager> reflowManagers) {
		Instant reference = instantOf(reflowManagers.get(0).message);
		TankReflowManager manager = reflowManagers.get(0);
		for (int i = 1; i < reflowManagers.size(); i++) {
			Instant comparable = instantOf(reflowManagers.get(i).message);
			if (comparable.isBefore(reference)) {
				reference = comparable;
				manager = reflowManagers.get(i);
			}
		}
		return manager;
	}

	private Instant instantOf(io.intino.ness.inl.Message message) {
		return message != null ? Instant.parse(message.ts()) : Instant.MAX;
	}

	private boolean flowsAreActive(List<TankReflowManager> reflowManagers) {
		for (TankReflowManager reflowManager : reflowManagers) if (reflowManager.message != null) return true;
		return false;
	}

	private List<TankReflowManager> reflowManagers(List<Tank> tanks, Session session) {
		List<TankReflowManager> reflowManagers = new ArrayList<>();
		for (Tank tank : tanks) {
			datalakeManager.stopFeed(tank);
			reflowManagers.add(new TankReflowManager(tank, session));
		}
		return reflowManagers;
	}

	private class TankReflowManager {

		private final Tank tank;
		private final TopicProducer producer;
		private final NessStation.Pump pump;
		private io.intino.ness.inl.Message message;

		TankReflowManager(Tank tank, Session session) {
			try {
				this.tank = tank;
				this.producer = new TopicProducer(session, tank.flowQN());
				this.pump = station.pump(tank.qualifiedName()).to(m -> message = m);
				this.pump.step();
			} catch (JMSException e) {
				throw new RuntimeException(e.getMessage());
			}
		}

		boolean step() {
			if (message == null || pump.step()) return true;
			pump.terminate();
			message = null;
			return false;
		}

		public void send() {
			producer.produce(createMessageFor(message.toString()));
			step();
		}
	}
}