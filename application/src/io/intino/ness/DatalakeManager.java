package io.intino.ness;

import io.intino.konos.jms.Consumer;
import io.intino.konos.jms.TopicConsumer;
import io.intino.ness.bus.BusManager;
import io.intino.ness.datalake.FileStation;
import io.intino.ness.datalake.Job;
import io.intino.ness.datalake.NessStation;
import io.intino.ness.datalake.NessStation.Feed;
import io.intino.ness.datalake.Valve;
import io.intino.ness.datalake.compiler.Compiler;
import io.intino.ness.inl.MessageFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Message;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.intino.konos.jms.MessageFactory.createMessageFor;
import static io.intino.ness.Inl.load;
import static java.lang.String.CASE_INSENSITIVE_ORDER;


public class DatalakeManager {
	private Logger logger = LoggerFactory.getLogger(DatalakeManager.class);
	private NessStation station;
	private BusManager bus;
	private List<Job> jobs = new ArrayList<>();

	public DatalakeManager(FileStation station, BusManager bus) {
		this.station = station;
		this.bus = bus;
		init();
	}

	private void init() {
		for (io.intino.ness.datalake.Tank tank : station.tanks()) {
			feed("feed." + tank.name(), station.feed(tank.name()));
			flow(tank.name(), "flow." + tank.name());
		}
	}

	public boolean isCorrect(String code) {
		return compile(code) != null;
	}

	private MessageFunction compile(String code) {
		try {
			return Compiler.
					compile(code).
					with("-target", "1.8").
					load("tests.UpperCaseFunction").
					as(MessageFunction.class).
					newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			return null;
		}
	}

	public void pump(Function function, String input, String output) {
		try {
			station.pipe(input).to(output).with(Valve.define().filter(function.name(), function.source()));
			Job job = station.pump(input).to(output).start();
			jobs.add(job);
			job.thread().start();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void registerTank(Tank tank) {
		station.tank(tank.qualifiedName);
	}

	public void feedFlow(Tank tank) {
		feed(tank, station.feed(tank.qualifiedName));
		flow(tank);
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
		station.flow(tank.qualifiedName).to(m -> bus.registerOrGetProducer(tank.flowQN()).produce(createMessageFor(m.toString())));
	}

	private void flow(String tank, String flow) {
		station.flow(tank).to(m -> bus.registerOrGetProducer(flow).produce(createMessageFor(m.toString())));
	}

	public void reflow(Tank tank) {
		stopFeed(tank);
		station.pump(tank.qualifiedName).to(m -> bus.registerOrGetProducer(tank.flowQN()).produce(createMessageFor(m.toString())));
	}

	public void migrate(Tank oldTank, Tank newTank, List<Function> functions) throws Exception {
		registerTank(newTank);
		stopFeed(oldTank);
		NessStation.Pipe pipe = station.pipe(oldTank.qualifiedName());
		for (Function function : functions) pipe = pipe.with(Valve.define().map(function.name(), function.source()));
		pipe.to(newTank.qualifiedName());
		Job job = station.pump(oldTank.qualifiedName()).to(newTank.qualifiedName()).start();
		jobs.add(job);
		job.onTerminate(() -> feedFlow(newTank));
	}

	private void stopFeed(Tank tank) {
		TopicConsumer consumer = bus.consumerOf(tank.feedQN());
		if (consumer != null) consumer.stop();
	}

	public String addUser(String name, List<String> groups) {
		return bus.addUser(name, groups);
	}

	public boolean removeUser(String name) {
		return bus.removeUser(name);
	}

	public void seal(Tank tank) {
		stopFeed(tank);
		Job seal = station.seal(tank.qualifiedName);
		seal.onTerminate(() -> feed(tank, station.feed(tank.qualifiedName)));
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

	public Map<String, List<String>> users() {
		return bus.users();
	}

	public List<String> topics() {
		return bus.topics().stream().sorted(CASE_INSENSITIVE_ORDER::compare).collect(Collectors.toList());
	}

	public void quit() {
		jobs.forEach(Job::stop);
		bus.quit();
	}

	public boolean rename(Tank tank, String name) {
		station.remove(station.feedsTo(tank.qualifiedName()));
		station.rename(tank.qualifiedName, name);
		return false;
	}
}
