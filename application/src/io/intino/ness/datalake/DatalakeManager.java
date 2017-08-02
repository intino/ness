package io.intino.ness.datalake;

import io.intino.konos.jms.Consumer;
import io.intino.konos.jms.TopicConsumer;
import io.intino.ness.bus.AqueductManager;
import io.intino.ness.bus.BusManager;
import io.intino.ness.datalake.NessStation.Feed;
import io.intino.ness.graph.Aqueduct;
import io.intino.ness.graph.Function;
import io.intino.ness.graph.Tank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Message;
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
	private Map<Aqueduct, AqueductManager> runningAqueducts = new HashMap<>();

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

	public void feed(Tank tank) {
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

	public void stopFeed(Tank tank) {
		TopicConsumer consumer = bus.consumerOf(tank.feedQN());
		if (consumer != null) consumer.stop();
	}

	public void reflow(List<Tank> tanks) {
		new ReflowProcess(this, bus, station).start(tanks);
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
		AqueductManager manager = new AqueductManager(aqueduct, bus);
		manager.start();
		runningAqueducts.put(aqueduct, manager);
		LoggerFactory.getLogger(this.getClass()).info("Aqueduct started: " + aqueduct.name$());
	}

	public void stopAqueduct(Aqueduct aqueduct) {
		AqueductManager aqueductManager = runningAqueducts.get(aqueduct);
		if (aqueductManager != null) {
			aqueductManager.stop();
			runningAqueducts.remove(aqueduct);
		}
	}

	public boolean status(Aqueduct aqueduct) {
		return runningAqueducts.get(aqueduct) != null;
	}
}
