package systems.intino.eventsourcing.datahubterminal;

import io.intino.alexandria.logger.Logger;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageNotWriteableException;
import jakarta.jms.TextMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import systems.intino.eventsourcing.datahubterminal.remotedatalake.DatalakeAccessor;
import systems.intino.eventsourcing.event.Event;
import systems.intino.eventsourcing.jms.ConnectionConfig;

import java.io.File;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class StubConnector implements Connector {
	private final Map<String, List<Consumer<Event>>> eventConsumers;
	private final Map<String, List<MessageConsumer>> messageConsumers;
	private final ConnectionConfig config;
	private final Map<String, String> urlParameters;
	private EventOutBox eventOutBox;
	private MessageOutBox messageOutBox;

	public StubConnector(ConnectionConfig config, File outBoxDirectory) {
		this.config = config;
		urlParameters = parameters(config.url());
		eventConsumers = new HashMap<>();
		messageConsumers = new HashMap<>();
		if (outBoxDirectory != null) {
			this.eventOutBox = new EventOutBox(new File(outBoxDirectory, "events"));
			this.messageOutBox = new MessageOutBox(new File(outBoxDirectory, "requests"));
		}
	}

	@Override
	public String clientId() {
		return "mock-client";
	}

	public void start() {
	}

	@Override
	public synchronized void sendEvent(String path, Event event) {
		ArrayList<Consumer<Event>> consumers = new ArrayList<>(eventConsumers.getOrDefault(path, Collections.emptyList()));
		for (Consumer<Event> c : consumers) c.accept(event);
		if (eventOutBox != null) eventOutBox.push(path, event);
	}

	public synchronized void sendEvents(String path, List<Event> events) {
		ArrayList<Consumer<Event>> consumers = new ArrayList<>(eventConsumers.getOrDefault(path, Collections.emptyList()));
		consumers.forEach(events::forEach);
		if (eventOutBox != null) events.forEach(e -> eventOutBox.push(path, e));
	}

	public synchronized void sendEvents(String path, List<Event> events, int expirationInSeconds) {
		sendEvents(path, events);
	}

	@Override
	public synchronized void sendEvent(String path, Event event, int expirationInSeconds) {
		sendEvent(path, event);
	}

	@Override
	public void attachListener(String path, Consumer<Event> onEventReceived) {
		registerEventConsumer(path, onEventReceived);
	}

	@Override
	public void attachListener(String path, Consumer<Event> onEventReceived, String messageSelector) {
		registerEventConsumer(path, onEventReceived);

	}

	@Override
	public void sendQueueMessage(String path, String message) {
		if (messageOutBox != null) messageOutBox.push(path, message);
	}

	@Override
	public void sendTopicMessage(String path, String message) {
		if (messageOutBox != null) messageOutBox.push(path, message);
	}

	@Override
	public void attachListener(String path, String subscriberId, Consumer<Event> onEventReceived) {
		registerEventConsumer(path, onEventReceived);
	}

	@Override
	public void attachListener(String path, String subscriberId, Consumer<Event> onEventReceived, Predicate<Instant> filter, String messageSelector) {
		registerEventConsumer(path, onEventReceived);
	}

	@Override
	public void attachListener(String path, String subscriberId, Consumer<Event> onEventReceived, String messageSelector) {
		registerEventConsumer(path, onEventReceived);
	}

	@Override
	public void attachListener(String path, MessageConsumer onMessageReceived) {
		registerMessageConsumer(path, onMessageReceived);

	}

	@Override
	public void attachListener(String path, String subscriberId, MessageConsumer onMessageReceived) {
		registerMessageConsumer(path, onMessageReceived);
	}


	@Override
	public void detachListeners(Consumer<Event> consumer) {
		eventConsumers.values().forEach(list -> list.remove(consumer));
	}

	@Override
	public void detachListeners(MessageConsumer consumer) {
		messageConsumers.values().forEach(list -> list.remove(consumer));
	}

	@Override
	public void createSubscription(String path, String subscriberId) {
	}

	public void destroySubscription(String subscriberId) {
	}

	@Override
	public void detachListeners(String path) {
		this.eventConsumers.get(path).clear();
		this.messageConsumers.get(path).clear();
	}

	@Override
	public jakarta.jms.Message requestResponse(String path, jakarta.jms.Message message) {
		return null;
	}

	@Override
	public jakarta.jms.Message requestResponse(String path, jakarta.jms.Message message, long timeout, TimeUnit timeUnit) {
		return path.equals(DatalakeAccessor.PATH) && isDatalakeRequest(message) ? datalakeArgument() : null;
	}

	private Message datalakeArgument() {
		try {
			String datalake = urlParameters.get("datalake");
			if (datalake == null) return null;
			createDirectoryIfNotExists(datalake);
			ActiveMQTextMessage message;
			message = new ActiveMQTextMessage();
			message.setText(datalake);
			return message;
		} catch (MessageNotWriteableException e) {
			Logger.error(e);
			return null;
		}
	}

	private void createDirectoryIfNotExists(String datalake) {
		try {
			new File(datalake).mkdirs();
		} catch (Exception e) {
			Logger.error("Could not create directory " + datalake + ": " + e.getMessage(), e);
		}
	}

	private Map<String, String> parameters(String url) {
		if (!url.contains("?")) return Map.of();
		String substring = url.substring(url.indexOf("?") + 1);
		return Arrays.stream(substring.split(";")).collect(Collectors.toMap(p -> p.split("=")[0], p -> p.split("=")[1]));
	}

	private boolean isDatalakeRequest(Message message) {
		if (!(message instanceof TextMessage)) return false;
		try {
			String text = ((TextMessage) message).getText();
			return text.startsWith("[Datalake]");
		} catch (JMSException e) {
			Logger.error(e);
			return false;
		}
	}

	@Override
	public void requestResponse(String path, jakarta.jms.Message message, String responsePath) {
	}

	@Override
	public long defaultTimeoutAmount() {
		return 0;
	}

	@Override
	public TimeUnit defaultTimeoutUnit() {
		return TimeUnit.SECONDS;
	}

	public void stop() {
	}

	private void registerEventConsumer(String path, Consumer<Event> onEventReceived) {
		this.eventConsumers.putIfAbsent(path, new CopyOnWriteArrayList<>());
		this.eventConsumers.get(path).add(onEventReceived);
	}

	private void registerMessageConsumer(String path, MessageConsumer onMessageReceived) {
		this.messageConsumers.putIfAbsent(path, new CopyOnWriteArrayList<>());
		this.messageConsumers.get(path).add(onMessageReceived);
	}
}