package io.intino.ness;

import io.intino.konos.jms.Consumer;
import io.intino.konos.jms.MessageFactory;
import io.intino.konos.jms.TopicProducer;
import io.intino.ness.bus.BusManager;
import io.intino.ness.datalake.FilePumpingStation;
import io.intino.ness.datalake.NessCompiler;
import io.intino.ness.datalake.NessPumpingStation;
import io.intino.ness.datalake.NessPumpingStation.Pipe;
import io.intino.ness.inl.Inl;
import io.intino.ness.inl.MessageFunction;

import javax.jms.Message;
import java.util.*;

import static java.util.logging.Logger.getGlobal;

public class DatalakeManager {

	private NessPumpingStation station;
	private BusManager bus;
	private List<NessPumpingStation.Task> tasks = new ArrayList<>();
	private Map<String, List<Pipe>> feeds = new HashMap<>();

	public DatalakeManager(FilePumpingStation station, BusManager bus) {
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
			station.pipe(input).with(function.name(), function.source).to(output);
			NessPumpingStation.Task task = station.pump(input);
			tasks.add(task);
			task.thread().start();
		} catch (Exception e) {
			getGlobal().severe(e.getMessage());

		}
	}

	public void registerChannel(Channel channel) {
		Pipe feed = station.feed(channel.feedQN());
		List<Pipe> pipes = feeds.putIfAbsent(channel.name(), Arrays.asList(feed));
		if (pipes != null) pipes.add(feed);
		bus.registerConsumer(channel.feedQN(), new Consumer() {
			@Override
			public void consume(Message message) {
				feed.send(Inl.messageOf(textFrom(message)));
			}
		});
		Pipe to = station.pipe(channel.feedQN()).to(new PipeProducer(channel.flowQN()));
		try {
			station.pump(channel.feedQN()).thread().join();
		} catch (Exception e) {
			getGlobal().severe(e.getMessage());
		}
	}

	public void reflow(Channel feed) {
//		new NessPump(dataLake).plug(feed).with(input -> input).into(flowTopicOf(feed));
	}

	public void migrate(Channel oldChannel, Channel newChannel) {
		registerChannel(newChannel);
		pauseFeed(oldChannel);
		Pipe pipe = station.pipe(oldChannel.feedQN()).to(newChannel.feedQN());
		try {
			station.pump(oldChannel.feedQN()).thread().start();
		} catch (Exception e) {
			getGlobal().severe(e.getMessage());
		}

	}

	private void pauseFeed(Channel channel) {
		List<Pipe> pipes = feeds.get(channel.feedQN());
		for (Pipe pipe : pipes) pipe.;
		pipes.clear();
	}

	public String addUser(String name, List<String> groups) {
		return bus.addUser(name, groups);
	}

	public boolean removeUser(String name) {
		return bus.removeUser(name);
	}

	private class PipeProducer implements Pipe {
		private TopicProducer topicProducer;

		public PipeProducer(String path) {
			topicProducer = bus.createProducer(path);
		}

		@Override
		public void send(io.intino.ness.inl.Message message) {
			topicProducer.produce(MessageFactory.createMessageFor(message.toString()));
		}
	}

}
