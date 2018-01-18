package io.intino.ness.datalake.reflow;

import io.intino.konos.jms.TopicProducer;
import io.intino.ness.bus.BusManager;
import io.intino.ness.datalake.DatalakeManager;
import io.intino.ness.graph.Tank;
import io.intino.ness.inl.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Session;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.intino.konos.jms.MessageFactory.createMessageFor;
import static java.time.Instant.parse;
import static java.util.stream.Collectors.toMap;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;

class ReflowProcess {
	private static final String FLOW_PATH = "flow.ness.reflow";
	private static Logger logger = LoggerFactory.getLogger(ROOT_LOGGER_NAME);

	private final List<Tank> tanks;
	private DatalakeManager datalakeManager;
	private final Session session;
	private final Iterator<Job> jobs;
	private TopicProducer producers;

	ReflowProcess(DatalakeManager datalakeManager, BusManager bus, List<Tank> tanks, int blockSize) {
		this.datalakeManager = datalakeManager;
		this.session = bus.transactedSession();
		this.tanks = tanks;
		this.jobs = createPumping(blockSize);
		this.producers = createProducer();
	}

	private TopicProducer createProducer() {
		try {
			return new TopicProducer(session, FLOW_PATH);
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	void next() {
		if (!jobs.hasNext()) terminateReflow();
		else jobs.next().onTerminate(() -> {
			commit();
			if (!jobs.hasNext()) terminateReflow();
		});
	}

	private Iterator<Job> createPumping(int blockSize) {
		return new Iterator<Job>() {

			List<TankManager> managers = tanks.stream().map(t -> new TankManager(t.qualifiedName(), datalakeManager.sortedMessagesIterator(t))).collect(Collectors.toList());

			@Override
			public boolean hasNext() {
				return flowsAreActive(managers);
			}

			@Override
			public Job next() {
				return new Job() {

					int messageCounter = 0;

					@Override
					protected boolean init() {
						return true;
					}

					@Override
					protected boolean step() {
						if (!flowsAreActive(managers)) return false;
						TankManager manager = managerWithOldestMessage(managers);
						producers.produce(createMessageFor(manager.message.toString()));
						manager.next();
						return ++messageCounter < blockSize;
					}

					@Override
					public void terminate() {
						super.terminate();
					}

					private TankManager managerWithOldestMessage(List<TankManager> managers) {
						Instant reference = instantOf(managers.get(0).message);
						TankManager manager = managers.get(0);
						for (int i = 1; i < managers.size(); i++) {
							Instant comparable = instantOf(managers.get(i).message);
							if (comparable.isBefore(reference)) {
								reference = comparable;
								manager = managers.get(i);
							}
						}
						return manager;
					}

					private Instant instantOf(io.intino.ness.inl.Message message) {
						return message != null ? parse(message.ts()) : Instant.MAX;
					}
				};
			}
		};

	}

	public Session getSession() {
		return session;
	}

	private void commit() {
		try {
			session.commit();
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void terminateReflow() {
		try {
			commit();
			session.close();
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
		}
	}


	static class TankManager {
		final String source;
		private final Iterator<Message> faucet;
		private io.intino.ness.inl.Message message;

		TankManager(String source, Iterator<Message> faucet) {
			this.source = source;
			this.faucet = faucet;
			next();
		}

		private void next() {
			this.message = faucet.next();
		}
	}

	private boolean flowsAreActive(List<TankManager> managers) {
		for (TankManager manager : managers)
			if (manager.message != null) return true;
		return false;
	}

}