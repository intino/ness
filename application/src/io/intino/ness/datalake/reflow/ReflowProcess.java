package io.intino.ness.datalake.reflow;

import io.intino.konos.jms.TopicProducer;
import io.intino.ness.bus.BusManager;
import io.intino.ness.datalake.DatalakeManager;
import io.intino.ness.datalake.Job;
import io.intino.ness.datalake.NessStation;
import io.intino.ness.datalake.NessStation.Pumping;
import io.intino.ness.graph.Tank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Session;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static io.intino.konos.jms.MessageFactory.createMessageFor;
import static java.util.stream.Collectors.toMap;
import static org.apache.log4j.LogManager.getLogger;
import static org.apache.log4j.Logger.getRootLogger;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;

class ReflowProcess {
	private static Logger logger = LoggerFactory.getLogger(ROOT_LOGGER_NAME);

	private final DatalakeManager datalakeManager;
	private final NessStation station;
	private final List<Tank> tanks;
	private final Session session;
	private final Iterator<Job> jobs;
	private boolean finished;
	private Map<String, TopicProducer> producers = new HashMap<>();

	ReflowProcess(DatalakeManager datalakeManager, BusManager bus, List<Tank> tanks, Integer blockSize) {
		this.datalakeManager = datalakeManager;
		this.station = datalakeManager.station();
		this.session = bus.transactedSession();
		this.tanks = tanks;
		this.producers = this.tanks.stream().collect(toMap(Tank::qualifiedName, this::createProducer));
		this.jobs = createPumping(blockSize);
		this.finished = false;
		for (Tank tank : tanks) datalakeManager.stopFeed(tank);
	}

	private TopicProducer createProducer(Tank tank) {
		try {
			return new TopicProducer(session, tank.qualifiedName());
		} catch (JMSException e) {
			getLogger(ROOT_LOGGER_NAME).error(e.getMessage(), e);
			return null;
		}
	}

	private Iterator<Job> createPumping(Integer blockSize) {
		Pumping pumping = station.pump();
		tanks.forEach(t -> pumping.from(t.qualifiedName())
				.to(m -> producers.get(t.qualifiedName()).produce(createMessageFor(m))));
		return pumping.asJob(blockSize);
	}

	void next() {
		if (!jobs.hasNext()) terminateReflow();
		jobs.next().onTerminate(() -> {
			commit();
			if (jobs.hasNext()) terminateReflow();
		});
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