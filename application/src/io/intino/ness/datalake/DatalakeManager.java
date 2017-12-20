package io.intino.ness.datalake;

import io.intino.ness.graph.Function;
import io.intino.ness.graph.Tank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.stream;


public class DatalakeManager {
	private Logger logger = LoggerFactory.getLogger(DatalakeManager.class);
	private NessStation station;
	private List<Job> jobs = new ArrayList<>();

	public DatalakeManager(String stationFolder) {
		this.station = new FileStation(stationFolder);
	}

	public void addTank(Tank tank) {
		station.tank(tank.qualifiedName());
	}

	public void removeTank(Tank tank) {
		String qualifiedName = tank.qualifiedName();
		station.remove(station.pipesFrom(qualifiedName));
		station.remove(station.pipesTo(qualifiedName));
		if (stream(station.tanks()).anyMatch(t -> t.name().equals(qualifiedName))) station.remove(qualifiedName);
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

	public NessStation station() {
		return this.station;
	}
}
