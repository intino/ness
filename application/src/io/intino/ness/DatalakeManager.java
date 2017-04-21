package io.intino.ness;

import io.intino.konos.jms.Consumer;
import io.intino.konos.jms.TopicConsumer;
import io.intino.ness.bus.BusManager;
import io.intino.ness.datalake.FileStation;
import io.intino.ness.datalake.NessCompiler;
import io.intino.ness.datalake.NessStation;
import io.intino.ness.datalake.NessStation.Feed;
import io.intino.ness.datalake.Valve;
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
	private List<NessStation.Task> tasks = new ArrayList<>();

	public DatalakeManager(FileStation station, BusManager bus) {
		this.station = station;
		this.bus = bus;
	}

	public boolean isCorrect(String code) {
		return compile(code) != null;
	}

	private MessageFunction compile(String code) {
		try {
			return NessCompiler.
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
			NessStation.Task task = station.pump(input).to(output).start();
			tasks.add(task);
			task.thread().start();
		} catch (Exception e) {

		}
	}

	public void registerChannel(Channel channel) {
		feed(channel, station.feed(channel.qualifiedName));
		flow(channel);
	}

	private void feed(Channel channel, final Feed feed) {
		bus.registerConsumer(channel.feedQN(), new Consumer() {
			public void consume(Message message) {
				load(textFrom(message)).forEach(feed::send);
			}
		});
	}

	private void flow(Channel channel) {
		station.flow(channel.qualifiedName).onMessage(m -> bus.registerOrGetProducer(channel.flowQN()).produce(createMessageFor(m.toString())));
	}

	public void reflow(Channel channel) {
		pauseFeed(channel);
		station.pump(channel.qualifiedName).to(m -> bus.registerOrGetProducer(channel.flowQN()).produce(createMessageFor(m.toString())));
	}

	public void migrate(Channel oldChannel, Channel newChannel) {
		registerChannel(newChannel);
		pauseFeed(oldChannel);
		station.pipe(oldChannel.feedQN()).to(newChannel.feedQN());
		NessStation.Task task = station.pump(oldChannel.feedQN()).start();
		tasks.add(task);
		task.thread().start();
		//onTerminate feed(channel,
	}

	private void pauseFeed(Channel channel) {
		TopicConsumer consumer = bus.consumerOf(channel.feedQN());
		if (consumer != null) consumer.stop();
	}

	public String addUser(String name, List<String> groups) {
		return bus.addUser(name, groups);
	}

	public boolean removeUser(String name) {
		return bus.removeUser(name);
	}
}
