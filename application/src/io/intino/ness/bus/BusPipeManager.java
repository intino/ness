package io.intino.ness.bus;

import io.intino.konos.jms.TopicConsumer;
import io.intino.ness.graph.BusPipe;
import io.intino.ness.graph.ExternalBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Message;
import java.util.*;
import java.util.regex.Pattern;

import static io.intino.ness.bus.MessageSender.send;
import static io.intino.ness.graph.BusPipe.Direction.incoming;
import static java.util.stream.Collectors.toSet;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;


public class BusPipeManager {
	private static final Logger logger = LoggerFactory.getLogger(ROOT_LOGGER_NAME);

	private final Map<BusPipe, List<String>> busPipes;
	private final List<TopicConsumer> internalConsumers;
	private final BusManager busManager;
	private ExternalBus externalBus;
	private boolean running;

	public BusPipeManager(BusManager busManager, ExternalBus externalBus) {
		this.busManager = busManager;
		this.internalConsumers = new ArrayList<>();
		this.busPipes = new HashMap<>();
		this.externalBus = externalBus;
	}

	public void addPipe(BusPipe pipe) {
		this.externalBus.initSession(busManager.nessID());
		busPipes.put(pipe, targetDestinations(pipe));
	}

	public void start() {
		for (BusPipe busPipe : busPipes.keySet()) start(busPipe);
	}

	public void start(BusPipe pipe) {
		if (pipe.direction().equals(incoming)) incomingPipe(pipe);
		else outgoingPipe(pipe);
		this.running = true;
	}

	public void stop() {
		internalConsumers.forEach(TopicConsumer::stop);
		internalConsumers.clear();
		externalBus.close();
		this.running = false;
	}

	public boolean isRunning() {
		return running;
	}

	private void incomingPipe(BusPipe pipe) {
		for (String topic : busPipes.get(pipe)) {
			if (externalBus.consumers().keySet().contains(topic)) {
				logger.info("topic already registered: " + topic);
				continue;
			}
			TopicConsumer consumer = externalBus.addConsumer(topic);
			consumer.listen(m -> incomingMessage(topic, m, pipe).start(), busManager.nessID() + "-" + topic);
		}
	}

	private void outgoingPipe(BusPipe pipe) {
		for (String topic : busPipes.get(pipe)) {
			TopicConsumer consumer = new TopicConsumer(busManager.nessSession(), topic);
			internalConsumers.add(consumer);
			consumer.listen(m -> outgoingMessage(topic, m, pipe).start());
		}
	}

	private Thread incomingMessage(String topic, Message m, BusPipe pipe) {
		return new Thread(() -> send(busManager.nessSession(), topic, m, pipe.transformer()));
	}

	private Thread outgoingMessage(String topic, Message m, BusPipe pipe) {
		return new Thread(() -> {
			checkExternalSession(pipe);
			send(externalBus.session(), topic, m, pipe.transformer());
		});
	}

	private void checkExternalSession(BusPipe pipe) {
		if (externalBus.sessionIsClosed()) {
			externalBus.reload();
			incomingPipe(pipe);
		}
	}

	private List<String> targetDestinations(BusPipe busPipe) {
		List<String> externalBusTopics = filter(externalBus.topics(), busPipe.tankMacro());
		return busPipe.direction().equals(incoming) ?
				filter(externalBusTopics, busPipe.tankMacro()) :
				filter(merge(busManager.topics(), externalBusTopics), busPipe.tankMacro());
	}

	private List<String> filter(Collection<String> topics, String macro) {
		Pattern pattern = Pattern.compile(macro);
		return new ArrayList<>(topics.stream().filter(t -> pattern.matcher(t).matches()).collect(toSet()));
	}

	private List<String> merge(List<String> topics, List<String> externalBusTopics) {
		HashSet<String> objects = new HashSet<>(topics);
		objects.addAll(externalBusTopics);
		return new ArrayList<>(objects);
	}

	public ExternalBus externalBus() {
		return externalBus;
	}

	public List<BusPipe> busPipes() {
		return new ArrayList<>(busPipes.keySet());
	}
}
