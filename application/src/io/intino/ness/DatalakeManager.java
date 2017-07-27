package io.intino.ness;

import io.intino.konos.jms.Consumer;
import io.intino.konos.jms.TopicConsumer;
import io.intino.konos.jms.TopicProducer;
import io.intino.ness.bus.AqueductManager;
import io.intino.ness.bus.BusManager;
import io.intino.ness.datalake.FileStation;
import io.intino.ness.datalake.Job;
import io.intino.ness.datalake.NessStation;
import io.intino.ness.datalake.NessStation.Feed;
import io.intino.ness.datalake.Valve;
import io.intino.ness.datalake.compiler.Compiler;
import io.intino.ness.graph.Aqueduct;
import io.intino.ness.graph.Function;
import io.intino.ness.graph.Tank;
import io.intino.ness.inl.MessageFunction;
import io.intino.ness.inl.TextMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.intino.konos.jms.MessageFactory.createMessageFor;
import static io.intino.ness.Inl.load;


public class DatalakeManager {
	private Logger logger = LoggerFactory.getLogger(DatalakeManager.class);
	private NessStation station;
	private BusManager bus;
	private List<Job> jobs = new ArrayList<>();
	private Map<Aqueduct, AqueductManager> aqueducts = new HashMap<>();

	public DatalakeManager(FileStation station, BusManager bus) {
		this.station = station;
		this.bus = bus;
		init();
	}

	private void init() {
		for (io.intino.ness.datalake.Tank tank : station.tanks()) {
			startFeedFlow(tank);
			LoggerFactory.getLogger(this.getClass()).info("Tank started: " + tank.name());
		}
	}

	private void startFeedFlow(io.intino.ness.datalake.Tank tank) {
		feed("feed." + tank.name(), station.feed(tank.name()));
		flow(tank.name(), "flow." + tank.name());
	}

	public String check(String className, String code) {
		try {
			return compile(className, code) != null ? "" : "";
		} catch (Compiler.Exception e) {
			return e.getMessage();
		}
	}

	private MessageFunction compile(String className, String code) {
		try {
			return Compiler.
					compile(code).
					with("-target", "1.8").
					load(className).
					as(TextMapper.class).
					newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			return null;
		}
	}

	public void pump(Function function, String input, String output) {
		try {
			station.pipe(input).to(output).with(Valve.define().filter(function.name$(), function.source()));
			Job job = station.pump(input).to(output).asJob();
			jobs.add(job);
			job.thread().start();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void registerTank(Tank tank) {
		station.tank(tank.qualifiedName());
	}

	public void feedFlow(Tank tank) {
		feed(tank);
		flow(tank);
	}

	private void feed(Tank tank) {
		feed(tank.feedQN(), station.feed(tank.qualifiedName()));
	}

	private void feed(Tank tank, final Feed feed) {
		feed(tank.feedQN(), feed);
	}

	private void feed(String tank, final Feed feed) {
		bus.registerConsumer(tank, new Consumer() {
			public void consume(Message message) {
				load(textFrom(message)).forEach(feed::send);
			}
		});
	}

	private void flow(Tank tank) {
		station.flow(tank.qualifiedName()).to(m -> bus.registerOrGetProducer(tank.flowQN()).produce(createMessageFor(m.toString())));
	}

	private void flow(String tank, String flow) {
		station.flow(tank).to(m -> bus.registerOrGetProducer(flow).produce(createMessageFor(m.toString())));
	}

	private void stopFeed(Tank tank) {
		TopicConsumer consumer = bus.consumerOf(tank.feedQN());
		if (consumer == null) return;
		consumer.stop();
	}

	public void reflow(List<Tank> tanks) {
		if(tanks.isEmpty()) return;
		Session session = bus.transactedSession();
		if (session == null) {
			logger.error("Impossible to create transacted session");
			return;
		}
		doReflow(tanks, session);
	}

	private void doReflow(List<Tank> tanks, Session session) {
		List<TankReflowManager> reflowManagers = reflowManagers(tanks, session);
		while(flowsAreActive(reflowManagers)) beforeFlow(reflowManagers).send();
		terminateReflow(session, reflowManagers);
	}

	private void terminateReflow(Session session, List<TankReflowManager> reflowManagers) {
		try {
			session.commit();
			session.close();
			for (TankReflowManager reflowManager : reflowManagers) feed(reflowManager.tank);
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private TankReflowManager beforeFlow(List<TankReflowManager> reflowManagers) {
		Instant reference = instantOf(reflowManagers.get(0).message);
		TankReflowManager manager = reflowManagers.get(0);
		for (int i = 1; i < reflowManagers.size(); i++) {
			Instant comparable = instantOf(reflowManagers.get(i).message);
			if(comparable.isBefore(reference)){
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
			stopFeed(tank);
			reflowManagers.add(new TankReflowManager(tank, session));
		}
		return reflowManagers;
	}

	public void migrate(Tank oldTank, Tank newTank, List<Function> functions) throws Exception {
		registerTank(newTank);
		stopFeed(oldTank);
		NessStation.Pipe pipe = station.pipe(oldTank.qualifiedName());
		for (Function function : functions) pipe = pipe.with(Valve.define().map(function.name$(), function.source()));
		pipe.to(newTank.qualifiedName());
		Job job = station.pump(oldTank.qualifiedName()).to(newTank.qualifiedName()).asJob();
		jobs.add(job);
	}

	public void seal(Tank tank) {
		stopFeed(tank);
		Job seal = station.seal(tank.qualifiedName());
		seal.onTerminate(() -> feed(tank, station.feed(tank.qualifiedName())));
	}

	public void removeTank(Tank tank) {
		String qualifiedName = tank.qualifiedName();
		bus.consumerOf(tank.feedQN()).stop();
		station.remove(station.feedsTo(qualifiedName));
		station.remove(station.flowsFrom(qualifiedName));
		station.remove(station.pipesFrom(qualifiedName));
		station.remove(station.pipesTo(qualifiedName));
		station.remove(qualifiedName);
	}

	public void quit() {
		jobs.forEach(Job::stop);
		bus.quit();
	}

	public boolean rename(Tank tank, String name) {
		station.remove(station.feedsTo(tank.qualifiedName()));
		station.rename(tank.qualifiedName(), name);
		return false;
	}

	public void startAqueduct(Aqueduct aqueduct) {
		AqueductManager manager = new AqueductManager(aqueduct, bus.nessSession());
		manager.start();
		aqueducts.put(aqueduct, manager);
		LoggerFactory.getLogger(this.getClass()).info("Aqueduct started: " + aqueduct.name$());
	}

	public void stopAqueduct(Aqueduct aqueduct) {
		AqueductManager aqueductManager = aqueducts.get(aqueduct);
		aqueductManager.stop();
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
