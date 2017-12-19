package io.intino.ness.datalake;

import io.intino.konos.jms.TopicConsumer;
import io.intino.ness.bus.BusManager;
import io.intino.ness.bus.BusService;
import io.intino.ness.graph.Function;
import io.intino.ness.graph.NessGraph;
import io.intino.ness.graph.Tank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static io.intino.konos.jms.Consumer.textFrom;
import static io.intino.ness.Inl.load;
import static java.util.Arrays.stream;


public class DatalakeManager {
	private Logger logger = LoggerFactory.getLogger(DatalakeManager.class);
	private final NessGraph graph;
	private NessStation station;
	private BusManager busManager;
	private final BusService busService;
	private List<Job> jobs = new ArrayList<>();

	public DatalakeManager(NessGraph graph, String stationFolder, BusManager busManager, BusService busService) {
		this.graph = graph;
		this.station = new FileStation(stationFolder);
		this.busManager = busManager;
		this.busService = busService;
		init();
	}

	public void registerTank(Tank tank) {
		station.tank(tank.qualifiedName());
		startTank(tank);
	}

	public void removeTank(Tank tank) {
		String qualifiedName = tank.qualifiedName();
		List<TopicConsumer> consumers = busManager.consumersOf(tank.feedQN());
		if (!consumers.isEmpty()) consumers.forEach(TopicConsumer::stop);
		station.remove(station.pipesFrom(qualifiedName));
		station.remove(station.pipesTo(qualifiedName));
		if (stream(station.tanks()).anyMatch(t -> t.name().equals(qualifiedName))) station.remove(qualifiedName);
	}

	public void startTank(Tank tank) {
		busManager.pipe(tank.feedQN(), tank.dropQN());
		busManager.registerConsumer(tank.dropQN(), message ->
				load(textFrom(message)).forEach(station.drop(tank.qualifiedName())::register));
	}

	public void stopTank(Tank tank) {
		busManager.stopPipe(tank.feedQN(), tank.dropQN());
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

	public void seal(Tank... tanks) {
		for (Tank tank : tanks) seal(tank);
	}

	private void seal(Tank tank) {
		station.seal(tank.qualifiedName()).init();
	}

	public void stop() {
		jobs.forEach(Job::stop);
	}

	public boolean rename(Tank tank, String name) {
		station.remove(station.feedsTo(tank.qualifiedName()));
		station.rename(tank.qualifiedName(), name);
		return false;
	}

	public void busManager(BusManager busManager) {
		this.busManager = busManager;
	}

	private void init() {
		for (Tank tank : graph.tankList()) {
			startTank(tank);
			logger.info("Tank started: " + tank.name$());
		}
	}

	public NessStation station() {
		return this.station;
	}
}
