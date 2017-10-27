package io.intino.ness.datalake;

import io.intino.konos.jms.TopicConsumer;
import io.intino.ness.bus.BusManager;
import io.intino.ness.bus.BusPipeManager;
import io.intino.ness.datalake.NessStation.Drop;
import io.intino.ness.datalake.NessStation.Feed;
import io.intino.ness.graph.BusPipe;
import io.intino.ness.graph.Function;
import io.intino.ness.graph.Pipe;
import io.intino.ness.graph.Tank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Session;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.intino.konos.jms.Consumer.textFrom;
import static io.intino.konos.jms.MessageFactory.createMessageFor;
import static io.intino.ness.Inl.load;
import static io.intino.ness.bus.MessageSender.send;
import static java.util.Arrays.stream;


public class DatalakeManager {
	private Logger logger = LoggerFactory.getLogger(DatalakeManager.class);
	private NessStation station;
	private BusManager bus;
	private List<Job> jobs = new ArrayList<>();
	private Map<BusPipe, BusPipeManager> runningBusPipes = new HashMap<>();

	public DatalakeManager(FileStation station, BusManager bus) {
		this.station = station;
		this.bus = bus;
		init();
	}

	public void registerTank(Tank tank) {
		station.tank(tank.qualifiedName());
	}

	public void feedFlow(Tank tank) {
		feed(tank);
		flow(tank);
		drop(tank);
	}

	public void feed(Tank tank) {
		feed(tank.feedQN(), station.feed(tank.qualifiedName()));
	}

	public void drop(Tank tank) {
		drop(tank.dropQN(), station.drop(tank.qualifiedName()));
	}

	public void stopFeed(Tank tank) {
		List<TopicConsumer> topicConsumers = bus.consumersOf(tank.feedQN());
		topicConsumers.forEach(TopicConsumer::stop);
		topicConsumers.clear();
	}

	public boolean pipe(Pipe pipe) {
		return pipe(pipe.origin(), pipe.destination(), pipe.transformer());
	}

	public boolean pipe(String from, String to, Function function) {
		try {
			if (stream(station.tanks()).anyMatch(t -> t.name().equals(from))) {
				Valve valve = function == null ? Valve.define() : Valve.define().filter(function.name$(), function.source());
				return station.pipe(from).to(to).with(valve) != null;
			}
			return pipeTopic(from, to, function);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}
	}

	private boolean pipeTopic(String from, String to, Function function) {
		Session ness = bus.nessSession();
		boolean isTank = stream(station.tanks()).anyMatch(t -> t.name().equals(to));
		bus.registerConsumer(from, m -> send(ness, (isTank ? "feed." : "") + to, m, function));
		return true;
	}

	public void pump(String from, String to, Function function) {
		try {
			Valve valve = function == null ? Valve.define() : Valve.define().filter(function.name$(), function.source());
			station.pipe(from).to(to).with(valve);
			Job job = station.pump(from).to(to).asJob();
			jobs.add(job);
			job.thread().start();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
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

	public void seal(Tank... tanks) {
		for (Tank tank : tanks) seal(tank);
	}

	private void seal(Tank tank) {
		stopFeed(tank);
		Job seal = station.seal(tank.qualifiedName());
		seal.onTerminate(() -> feed(tank, station.feed(tank.qualifiedName())));
	}

	public void removeTank(Tank tank) {
		String qualifiedName = tank.qualifiedName();
		List<TopicConsumer> consumers = bus.consumersOf(tank.feedQN());
		if (!consumers.isEmpty()) consumers.forEach(TopicConsumer::stop);
		station.remove(station.feedsTo(qualifiedName));
		station.remove(station.flowsFrom(qualifiedName));
		station.remove(station.pipesFrom(qualifiedName));
		station.remove(station.pipesTo(qualifiedName));
		if (stream(station.tanks()).anyMatch(t -> t.name().equals(qualifiedName))) station.remove(qualifiedName);
	}

	public void quit() {
		jobs.forEach(Job::stop);
		bus.stop();
	}

	public boolean rename(Tank tank, String name) {
		station.remove(station.feedsTo(tank.qualifiedName()));
		station.rename(tank.qualifiedName(), name);
		return false;
	}

	public void startBusPipe(BusPipe busPipe) {
		BusPipeManager manager = new BusPipeManager(busPipe, bus);
		manager.start();
		runningBusPipes.put(busPipe, manager);
		logger.info("Bus pipe started: " + busPipe.name$());
	}

	public void busManager(BusManager busManager) {
		this.bus = busManager;
	}

	public void stopBusPipe(BusPipe busPipe) {
		BusPipeManager busPipeManager = runningBusPipes.get(busPipe);
		if (busPipeManager != null) {
			busPipeManager.stop();
			runningBusPipes.remove(busPipe);
		}
	}

	public boolean status(BusPipe busPipe) {
		return runningBusPipes.get(busPipe) != null;
	}

	private void init() {
		for (io.intino.ness.datalake.Tank tank : station.tanks()) {
			startFeedFlow(tank);
			logger.info("Tank started: " + tank.name());
		}
	}

	private void startFeedFlow(io.intino.ness.datalake.Tank tank) {
		feed("feed." + tank.name(), station.feed(tank.name()));
		flow(tank.name(), "flow." + tank.name());
	}

	private void feed(Tank tank, final Feed feed) {
		feed(tank.feedQN(), feed);
	}

	private void drop(String tank, final Drop drop) {
		bus.registerConsumer(tank, message -> load(textFrom(message)).forEach(drop::register));
	}

	private void feed(String tank, final Feed feed) {
		bus.registerConsumer(tank, message -> load(textFrom(message)).forEach(feed::send));
	}

	private void flow(Tank tank) {
		station.flow(tank.qualifiedName()).to(m -> bus.registerOrGetProducer(tank.flowQN()).produce(createMessageFor(m.toString())));
	}

	private void flow(String tank, String flow) {
		station.flow(tank).to(m -> bus.registerOrGetProducer(flow).produce(createMessageFor(m.toString())));
	}

	public NessStation station() {
		return this.station;
	}
}
