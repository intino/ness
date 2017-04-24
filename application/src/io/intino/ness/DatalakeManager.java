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

import static io.intino.konos.jms.MessageFactory.createMessageFor;
import static io.intino.ness.Inl.load;


public class DatalakeManager {
	private Logger logger = LoggerFactory.getLogger(DatalakeManager.class);
	private NessStation station;
	private BusManager bus;
	private List<Job> jobs = new ArrayList<>();

	public DatalakeManager(FileStation station, BusManager bus) {
		this.station = station;
		this.bus = bus;
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

		}
	}

	public void registerTank(Tank tank) {
		feed(tank, station.feed(tank.qualifiedName));
		flow(tank);
	}

	private void feed(Tank tank, final Feed feed) {
		bus.registerConsumer(tank.feedQN(), new Consumer() {
			public void consume(Message message) {
				load(textFrom(message)).forEach(feed::send);
			}
		});
	}

	private void flow(Tank tank) {
		station.flow(tank.qualifiedName).to(m -> bus.registerOrGetProducer(tank.flowQN()).produce(createMessageFor(m.toString())));
	}

	public void reflow(Tank tank) {
		pauseFeed(tank);
		station.pump(tank.qualifiedName).to(m -> bus.registerOrGetProducer(tank.flowQN()).produce(createMessageFor(m.toString())));
	}

	public void migrate(Tank oldTank, Tank newTank) {
		registerTank(newTank);
		pauseFeed(oldTank);
		station.pipe(oldTank.feedQN()).to(newTank.feedQN());
		Job job = station.pump(oldTank.feedQN()).start();
		jobs.add(job);
		job.onTerminate(() -> feed(newTank, station.feed(newTank.qualifiedName)));
		job.thread().start();
		//Destroy old tank?
	}

	private void pauseFeed(Tank tank) {
		TopicConsumer consumer = bus.consumerOf(tank.feedQN());
		if (consumer != null) consumer.stop();
	}

	public String addUser(String name, List<String> groups) {
		return bus.addUser(name, groups);
	}

	public boolean removeUser(String name) {
		return bus.removeUser(name);
	}
}
