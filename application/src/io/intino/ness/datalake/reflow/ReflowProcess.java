package io.intino.ness.datalake.reflow;

import io.intino.konos.jms.TopicProducer;
import io.intino.ness.bus.BusManager;
import io.intino.ness.datalake.DatalakeManager;
import io.intino.ness.datalake.NessStation;
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
	private final List<TankReflowManager> reflowManagers;
	private final Session session;
	private boolean finished;
//	private ReflowManager dialogs;
//	private ReflowManager surveys;

	ReflowProcess(DatalakeManager datalakeManager, BusManager bus, List<Tank> tanks) {
		this.datalakeManager = datalakeManager;
		this.station = datalakeManager.station();
		this.session = bus.transactedSession();
		this.tanks = tanks;
		this.reflowManagers = reflowManagers(tanks);
		this.finished = false;
		// TODO check
//		dialogs = new ReflowManager(session, tanks.get(0), new File("/Users/oroncal/workspace/ness/temp/datalake/dialogs.v4"));
//		surveys = new ReflowManager(session, tanks.get(1), new File("/Users/oroncal/workspace/ness/temp/datalake/surveys.v4"));
//		dialogs = new ReflowManager(session, tanks.get(0), new File("/home/intino/.ness/datalake/dialogs.v4"));
//		surveys = new ReflowManager(session, tanks.get(1), new File("/home/intino/.ness/datalake/surveys.v4"));
	}

	void next(int size) {
		if (tanks.isEmpty()) return;
//		manualReflow(size);
		doReflow(size);
		getRootLogger().info("Reflowed " + size + " messages");
	}

	boolean finished() {
		return finished;
	}

	private void doReflow(int size) {
		int messageCounter = 0;
		while (flowsAreActive(reflowManagers)) {
			beforeFlow(reflowManagers).send();
			if (++messageCounter == size) {
				commit();
				return;
			}
		}
		terminateReflow();
	}

	private void commit() {
		try {
			session.commit();
		} catch (JMSException e) {
			getRootLogger().error(e.getMessage(), e);
		}
	}

	private void terminateReflow() {
		try {
			commit();
			session.close();
			this.finished = true;
			for (Tank tank : tanks) datalakeManager.feed(tank);
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
		return message != null ? parse(message.ts()) : Instant.MAX;
	}

	private boolean flowsAreActive(List<TankReflowManager> reflowManagers) {
		for (TankReflowManager reflowManager : reflowManagers) if (reflowManager.message != null) return true;
		return false;
	}

	private List<TankReflowManager> reflowManagers(List<Tank> tanks) {
		List<TankReflowManager> reflowManagers = new ArrayList<>();
		for (Tank tank : tanks) {
			datalakeManager.stopFeed(tank);
			reflowManagers.add(new TankReflowManager(tank));
		}
		return reflowManagers;
	}

	private class TankReflowManager {

		private final Tank tank;
		private final NessStation.Pump pump;
		private TopicProducer producer;
		private io.intino.ness.inl.Message message;

		TankReflowManager(Tank tank) {
			this.tank = tank;
			this.pump = station.pump(tank.qualifiedName()).to(m -> message = m);
			this.pump.step();
			createProducer();
		}

		void send() {
			producer.produce(createMessageFor(message.toString()));
			step();
		}

		boolean step() {
			if (message == null || pump.step()) return true;
			pump.terminate();
			message = null;
			return false;
		}

		private void createProducer() {
			try {
				this.producer = new TopicProducer(session, tank.flowQN(), 120);
			} catch (JMSException e) {
				throw new RuntimeException(e.getMessage());
			}
		}
	}
//
//
//	private void manualReflow(int size) {
//		int messageCounter = 0;
//		Message dialog = dialogs.next();
//		Message survey = surveys.next();
//		while (dialogs.hasNext() || surveys.hasNext()) {
//			if (survey == null || parse(dialog.ts()).isBefore(parse(survey.ts()))) {
//				dialogs.send(dialog);
//				dialog = dialogs.next();
//			} else if (survey != null) {
//				surveys.send(survey);
//				survey = surveys.next();
//			}
//			if (++messageCounter == size) {
//				commit();
//				return;
//			}
//		}
//		terminateReflow();
//	}
//
//	private class ReflowManager {
//		List<File> files = new ArrayList<>();
//		int fileIndex = -1;
//		int messageIndex = -1;
//		private List<Message> messages;
//		private Message currentMessage = new Message("init");
//		private TopicProducer producer;
//
//		public ReflowManager(Session session, Tank tank, File folder) {
//			createProducer(session, tank);
//			if (folder.listFiles() != null)
//				files.addAll(asList(folder.listFiles()).stream().filter(f -> f.getName().endsWith("inl")).collect(toList()));
//			messages = nextMessages();
//		}
//
//		void createProducer(Session session, Tank tank) {
//			try {
//				this.producer = new TopicProducer(session, tank.flowQN(), 120);
//			} catch (JMSException e) {
//				throw new RuntimeException(e.getMessage());
//			}
//		}
//
//		private List<Message> nextMessages() {
//			try {
//				if (fileIndex >= files.size() - 1) return emptyList();
//				Path path = files.get(++fileIndex).toPath();
//				return Inl.load(String.join("\n", Files.readAllLines(path)));
//			} catch (IOException e) {
//				e.printStackTrace();
//				return null;
//			}
//		}
//
//		private Message next() {
//			if (messageIndex >= messages.size() - 1) {
//				messages = nextMessages();
//				if (messages.isEmpty()) return currentMessage = null;
//				messageIndex = -1;
//			}
//			return currentMessage = messages.get(++messageIndex);
//		}
//
//		boolean hasNext() {
//			return currentMessage != null;
//		}
//
//		public void send(Message message) {
//			producer.produce(createMessageFor(message.toString()));
//		}
//	}
}