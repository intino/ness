package io.intino.ness.datalake.reflow;

import io.intino.konos.jms.TopicProducer;
import io.intino.ness.bus.BusManager;
import io.intino.ness.datalake.DatalakeManager;
import io.intino.ness.datalake.NessStation;
import io.intino.ness.datalake.NessStation.Pumping;
import io.intino.ness.graph.Tank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Session;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static io.intino.konos.jms.MessageFactory.createMessageFor;
import static java.time.Instant.parse;
import static org.apache.log4j.Logger.getRootLogger;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;

class ReflowProcess {
	private static Logger logger = LoggerFactory.getLogger(ROOT_LOGGER_NAME);

	private final DatalakeManager datalakeManager;
	private final NessStation station;
	private final List<Tank> tanks;
	private final Session session; // TODO not used
	private final Pumping pumping;
	private boolean finished;

	ReflowProcess(DatalakeManager datalakeManager, BusManager bus, List<Tank> tanks) {
		this.datalakeManager = datalakeManager;
		this.station = datalakeManager.station();
		this.session = bus.transactedSession();
		this.tanks = tanks;
		this.pumping = createPumping();
		this.finished = false;
	}

	private Pumping createPumping() {
		Pumping pumping = station.pump();
		tanks.forEach(t -> pumping.from(t.qualifiedName()).to(t.qualifiedName()));
		return pumping;
	}

	void next(int size) {
		// TODO
	}

	boolean finished() {
		return finished;
	}

	private void commit() {
		try {
			session.commit();
		} catch (JMSException e) {
			getRootLogger().error(e.getMessage(), e);
		}
	}

	private void terminateReflow() {
		// TODO
		try {
			commit();
			session.close();
			this.finished = true;
			for (Tank tank : tanks) datalakeManager.feed(tank);
		} catch (JMSException e) {
			ReflowProcess.logger.error(e.getMessage(), e);
		}
	}

}