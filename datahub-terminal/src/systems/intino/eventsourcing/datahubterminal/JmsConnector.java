package systems.intino.eventsourcing.datahubterminal;

import io.intino.alexandria.logger.Logger;
import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.command.ActiveMQDestination;
import systems.intino.eventsourcing.event.Event;
import systems.intino.eventsourcing.jms.*;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static jakarta.jms.Session.AUTO_ACKNOWLEDGE;
import static jakarta.jms.Session.SESSION_TRANSACTED;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static systems.intino.eventsourcing.jms.MessageReader.textFrom;

public class JmsConnector implements Connector {
	private final Map<String, JmsProducer> producers;
	private final Map<String, JmsConsumer> consumers;
	private final Map<String, List<Consumer<Event>>> eventConsumers;
	private final Map<String, List<MessageConsumer>> messageConsumers;
	private final Map<Consumer<Event>, Integer> jmsEventConsumers;
	private final Map<MessageConsumer, Integer> jmsMessageConsumers;
	private final ConnectionConfig config;
	private final boolean transactedSession;
	private final AtomicBoolean connected = new AtomicBoolean(false);
	private final AtomicBoolean started = new AtomicBoolean(false);
	private EventOutBox eventOutBox;
	private MessageOutBox messageOutBox;
	private Connection connection;
	private Session session;
	private ScheduledExecutorService scheduler;
	private final ExecutorService eventDispatcher;
	private final ExecutorService messageDispatcher;
	private TemporaryQueue temporaryQueue;

	public JmsConnector(ConnectionConfig config, File outboxDirectory) {
		this(config, false, outboxDirectory);
	}

	public JmsConnector(ConnectionConfig config, boolean transactedSession, File outBoxDirectory) {
		this.config = config;
		this.transactedSession = transactedSession;
		producers = new HashMap<>();
		consumers = new HashMap<>();
		jmsEventConsumers = new HashMap<>();
		jmsMessageConsumers = new HashMap<>();
		eventConsumers = new HashMap<>();
		messageConsumers = new HashMap<>();
		if (outBoxDirectory != null) {
			this.eventOutBox = new EventOutBox(new File(outBoxDirectory, "events"));
			this.messageOutBox = new MessageOutBox(new File(outBoxDirectory, "requests"));
		}
		eventDispatcher = Executors.newSingleThreadExecutor(new NamedThreadFactory("JmsConnector-events"));
		messageDispatcher = Executors.newSingleThreadExecutor(r -> new Thread(r, "JmsConnector-messages"));
	}

	@Override
	public String clientId() {
		return config.clientId();
	}

	public void start() {
		if (config.url() == null || config.url().isEmpty()) {
			Logger.warn("Invalid broker URL (" + config.url() + "). Connection aborted");
			return;
		}
		try {
			connect();
		} catch (JMSException e) {
			Logger.error(e);
		}
		started.set(true);
		if (scheduler == null) {
			scheduler = Executors.newScheduledThreadPool(1);
			scheduler.scheduleAtFixedRate(this::checkConnection, 15, 10, MINUTES);
		}
	}

	private void connect() throws JMSException {
		if (!Broker.isRunning(config.url())) {
			Logger.warn("Broker (" + config.url() + ") unreachable. Connection aborted");
			return;
		}
		initConnection();
		if (connection != null && ((ActiveMQConnection) connection).isStarted()) {
			clearProducers();
			session = createSession(transactedSession);
			if (session != null && ((ActiveMQSession) session).isRunning()) {
				connected.set(true);
				recoverEventsAndMessages();
			}
		}
	}

	@Override
	public synchronized void sendEvent(String path, Event event) {
		ArrayList<Consumer<Event>> consumers = new ArrayList<>(eventConsumers.getOrDefault(path, Collections.emptyList()));
		for (Consumer<Event> c : consumers) c.accept(event);
		eventDispatcher.execute(() -> {
			if (!doSendEvent(path, event) && eventOutBox != null) eventOutBox.push(path, event);
		});
	}


	public synchronized void sendEvents(String path, List<Event> events) {
		ArrayList<Consumer<Event>> consumers = new ArrayList<>(eventConsumers.getOrDefault(path, Collections.emptyList()));
		consumers.forEach(events::forEach);
		eventDispatcher.execute(() -> {
			if (!doSendEvents(path, events) && eventOutBox != null) events.forEach(e -> eventOutBox.push(path, e));
		});
	}

	public synchronized void sendEvents(String path, List<Event> events, int expirationInSeconds) {
		ArrayList<Consumer<Event>> consumers = new ArrayList<>(eventConsumers.getOrDefault(path, Collections.emptyList()));
		consumers.forEach(events::forEach);
		eventDispatcher.execute(() -> {
			if (!doSendEvents(path, events, expirationInSeconds) && eventOutBox != null)
				events.forEach(e -> eventOutBox.push(path, e));
		});
	}

	@Override
	public synchronized void sendEvent(String path, Event event, int expirationInSeconds) {
		ArrayList<Consumer<Event>> consumers = new ArrayList<>(eventConsumers.getOrDefault(path, Collections.emptyList()));
		for (Consumer<Event> eventConsumer : consumers) eventConsumer.accept(event);
		eventDispatcher.execute(() -> {
			if (!doSendEvent(path, event, expirationInSeconds) && eventOutBox != null) eventOutBox.push(path, event);
		});
	}

	@Override
	public void attachListener(String path, Consumer<Event> onEventReceived) {
		registerEventConsumer(path, null, onEventReceived);
		attach(path, onEventReceived);
	}

	@Override
	public void attachListener(String path, Consumer<Event> onEventReceived, String messageSelector) {
		registerEventConsumer(path, messageSelector, onEventReceived);
		attach(path, onEventReceived);
	}


	@Override
	public void sendQueueMessage(String path, String message) {
		recoverEventsAndMessages();
		if (!doSendMessageToQueue(path, message) && messageOutBox != null) messageOutBox.push(path, message);
	}

	@Override
	public void sendTopicMessage(String path, String message) {
		recoverEventsAndMessages();
		if (!doSendMessageToTopic(path, message) && messageOutBox != null) messageOutBox.push(path, message);
	}

	@Override
	public void attachListener(String path, String subscriberId, Consumer<Event> onEventReceived) {
		registerEventConsumer(path, subscriberId, null, onEventReceived);
		attach(path, onEventReceived);
	}

	@Override
	public void attachListener(String path, String subscriberId, Consumer<Event> onEventReceived, Predicate<Instant> filter, String messageSelector) {
		if (filter == null) attachListener(path, subscriberId, onEventReceived, messageSelector);
		registerEventConsumer(path, subscriberId, messageSelector, onEventReceived);
		JmsConsumer consumer = this.consumers.get(path);
		if (consumer == null) return;
		Consumer<jakarta.jms.Message> eventConsumer = m -> {
			final Instant timestamp = timestamp(m);
			if (timestamp != null && filter != null && filter.test(timestamp))
				MessageTranslator.deserialize(m).forEach(onEventReceived);
		};
		jmsEventConsumers.put(onEventReceived, eventConsumer.hashCode());
		consumer.listen(eventConsumer);
	}

	@Override
	public void attachListener(String path, String subscriberId, Consumer<Event> onEventReceived, String messageSelector) {
		registerEventConsumer(path, subscriberId, messageSelector, onEventReceived);
		attach(path, onEventReceived);
	}

	@Override
	public void attachListener(String path, MessageConsumer onMessageReceived) {
		registerMessageConsumer(path, onMessageReceived);
		attach(path, onMessageReceived);
	}

	@Override
	public void attachListener(String path, String subscriberId, MessageConsumer onMessageReceived) {
		registerMessageConsumer(path, subscriberId, onMessageReceived);
		attach(path, onMessageReceived);
	}

	private void attach(String path, MessageConsumer onMessageReceived) {
		JmsConsumer consumer = this.consumers.get(path);
		if (consumer == null) return;
		Consumer<jakarta.jms.Message> messageConsumer = m -> onMessageReceived.accept(textFrom(m), callback(m));
		jmsMessageConsumers.put(onMessageReceived, messageConsumer.hashCode());
		consumer.listen(messageConsumer);
	}

	private void attach(String path, Consumer<Event> onEventReceived) {
		JmsConsumer consumer = this.consumers.get(path);
		if (consumer == null) return;
		Consumer<jakarta.jms.Message> eventConsumer = m -> MessageTranslator.deserialize(m).forEach(onEventReceived);
		jmsEventConsumers.put(onEventReceived, eventConsumer.hashCode());
		consumer.listen(eventConsumer);
	}

	private Instant timestamp(jakarta.jms.Message m) {
		try {
			return Instant.ofEpochMilli(m.getJMSTimestamp());
		} catch (JMSException e) {
			Logger.error(e);
			return null;
		}
	}

	@Override
	public void detachListeners(Consumer<Event> consumer) {
		eventConsumers.values().forEach(list -> list.remove(consumer));
		detach(jmsEventConsumers.get(consumer));
	}

	@Override
	public void detachListeners(MessageConsumer consumer) {
		messageConsumers.values().forEach(list -> list.remove(consumer));
		detach(jmsMessageConsumers.get(consumer));
	}

	private void detach(Integer consumerCode) {
		if (consumerCode == null) return;
		for (JmsConsumer jc : consumers.values()) {
			List<Consumer<jakarta.jms.Message>> toRemove = jc.listeners().stream().filter(l -> l.hashCode() == consumerCode).collect(Collectors.toList());
			toRemove.forEach(jc::removeListener);
		}
	}

	@Override
	public void createSubscription(String path, String subscriberId) {
		if (session != null && !this.consumers.containsKey(path))
			this.consumers.put(path, durableTopicConsumer(path, subscriberId, null));
	}

	public void destroySubscription(String subscriberId) {
		try {
			session.unsubscribe(subscriberId);
		} catch (JMSException e) {
			Logger.error(e);
		}
	}

	@Override
	public void detachListeners(String path) {
		if (this.consumers.containsKey(path)) {
			this.consumers.get(path).close();
			this.consumers.remove(path);
			this.eventConsumers.get(path).clear();
			this.messageConsumers.get(path).clear();
		}
	}

	@Override
	public synchronized jakarta.jms.Message requestResponse(String path, jakarta.jms.Message message) {
		return requestResponse(path, message, config.defaultTimeoutAmount(), config.defaultTimeoutUnit());
	}

	@Override
	public synchronized jakarta.jms.Message requestResponse(String path, jakarta.jms.Message message, long timeout, TimeUnit timeUnit) {
		if (session == null) {
			Logger.error("Connection lost. Invalid session");
			return null;
		}
		try {
			QueueProducer producer = new QueueProducer(session, path);
			if (this.temporaryQueue == null) temporaryQueue = session.createTemporaryQueue();
			message.setJMSReplyTo(temporaryQueue);
			message.setJMSCorrelationID(createRandomString());
			try (jakarta.jms.MessageConsumer consumer = session.createConsumer(temporaryQueue)) {
				CompletableFuture<jakarta.jms.Message> future = new CompletableFuture<>();
				consumer.setMessageListener(future::complete);
				sendMessage(producer, message, 100);
				producer.close();
				Message response = waitFor(future, timeout, timeUnit);
				consumer.close();
				return response;
			}
		} catch (JMSException | ExecutionException | InterruptedException e) {
			if (e.getMessage() == null) Logger.error(e);
			else Logger.error(e.getMessage());
		} catch (TimeoutException e) {
			Logger.warn("Timeout receiving response of jms query");
		}
		return null;
	}

	private static jakarta.jms.Message waitFor(Future<jakarta.jms.Message> future, long timeout, TimeUnit timeUnit) throws ExecutionException, InterruptedException, TimeoutException {
		return timeout <= 0 || timeUnit == null ? future.get() : future.get(timeout, timeUnit);
	}


	@Override
	public void requestResponse(String path, jakarta.jms.Message message, String responsePath) {
		try {
			message.setJMSReplyTo(this.session.createQueue(responsePath));
			message.setJMSCorrelationID(createRandomString());
			sendMessage(producers.get(path), message);
		} catch (JMSException e) {
			Logger.error(e);
		}
	}

	@Override
	public long defaultTimeoutAmount() {
		return config.defaultTimeoutAmount();
	}

	@Override
	public TimeUnit defaultTimeoutUnit() {
		return config.defaultTimeoutUnit();
	}

	public Connection connection() {
		return connection;
	}

	public Session session() {
		return session;
	}

	public void stop() {
		try {
			consumers.values().forEach(JmsConsumer::close);
			consumers.clear();
			messageDispatcher.shutdown();
			messageDispatcher.awaitTermination(1, MINUTES);
			producers.values().forEach(JmsProducer::close);
			producers.clear();
			if (session != null) session.close();
			if (connection != null) connection.close();
			session = null;
			connection = null;
		} catch (Throwable e) {
			Logger.error(e);
		}
	}

	private Session createSession(boolean transactedSession) throws JMSException {
		return connection.createSession(transactedSession, transactedSession ? SESSION_TRANSACTED : AUTO_ACKNOWLEDGE);
	}

	private void registerEventConsumer(String path, String messageSelector, Consumer<Event> onEventReceived) {
		this.eventConsumers.putIfAbsent(path, new CopyOnWriteArrayList<>());
		this.eventConsumers.get(path).add(onEventReceived);
		if (session != null && !this.consumers.containsKey(path))
			this.consumers.put(path, topicConsumer(path, messageSelector));
	}

	private void registerEventConsumer(String path, String subscriberId, String messageSelector, Consumer<Event> onEventReceived) {
		this.eventConsumers.putIfAbsent(path, new CopyOnWriteArrayList<>());
		this.eventConsumers.get(path).add(onEventReceived);
		if (session != null && !this.consumers.containsKey(path))
			this.consumers.put(path, durableTopicConsumer(path, subscriberId, messageSelector));
	}

	private void registerMessageConsumer(String path, MessageConsumer onMessageReceived) {
		this.messageConsumers.putIfAbsent(path, new CopyOnWriteArrayList<>());
		this.messageConsumers.get(path).add(onMessageReceived);
		if (session != null && !this.consumers.containsKey(path)) this.consumers.put(path, queueConsumer(path));
	}

	private void registerMessageConsumer(String path, String subscriberId, MessageConsumer onMessageReceived) {
		this.messageConsumers.putIfAbsent(path, new CopyOnWriteArrayList<>());
		this.messageConsumers.get(path).add(onMessageReceived);
		if (session != null && !this.consumers.containsKey(path))
			this.consumers.put(path, subscriberId == null ? topicConsumer(path, null) : durableTopicConsumer(path, subscriberId, null));
	}

	private boolean doSendEvent(String path, Event event) {
		return doSendEvent(path, event, 0);
	}

	private boolean doSendEvents(String path, List<Event> event) {
		return doSendEvents(path, event, 0);
	}

	private boolean doSendEvent(String path, Event event, int expirationTimeInSeconds) {
		if (cannotSendMessage()) return false;
		try {
			return sendMessage(topicProducer(path), MessageTranslator.serialize(event), expirationTimeInSeconds);
		} catch (JMSException | IOException e) {
			Logger.error(e);
			return false;
		}
	}

	private boolean doSendEvents(String path, List<Event> events, int expirationTimeInSeconds) {
		if (cannotSendMessage()) return false;
		try {
			return sendMessage(topicProducer(path), MessageTranslator.serialize(events), expirationTimeInSeconds);
		} catch (JMSException | IOException e) {
			Logger.error(e);
			return false;
		}
	}

	private boolean doSendMessageToQueue(String path, String message) {
		try {
			if (cannotSendMessage()) return false;
			return sendMessage(queueProducer(path), MessageWriter.write(message));
		} catch (JMSException e) {
			Logger.error(e);
			return false;
		}
	}

	private boolean doSendMessageToTopic(String path, String message) {
		try {
			if (cannotSendMessage()) return false;
			return sendMessage(queueProducer(path), MessageWriter.write(message));
		} catch (JMSException e) {
			Logger.error(e);
			return false;
		}
	}

	private JmsProducer topicProducer(String path) throws JMSException {
		if (!producers.containsKey(path)) producers.put(path, new TopicProducer(session, path));
		return producers.get(path);
	}

	private JmsProducer queueProducer(String path) throws JMSException {
		if (!producers.containsKey(path)) producers.put(path, new QueueProducer(session, path));
		return producers.get(path);
	}

	private boolean cannotSendMessage() {
		return session == null || !connected.get();
	}

	private boolean sendMessage(JmsProducer producer, jakarta.jms.Message message) {
		return sendMessage(producer, message, 0);
	}

	private boolean sendMessage(JmsProducer producer, jakarta.jms.Message message, int expirationTimeInSeconds) {
		try {
			Future<Boolean> result = messageDispatcher.submit(() -> producer.produce(message, expirationTimeInSeconds));
			return result.get(1, SECONDS);
		} catch (InterruptedException | TimeoutException ignored) {
		} catch (ExecutionException e) {
			Logger.error(e);
		}
		return false;
	}

	private ConnectionListener connectionListener() {
		return new ConnectionListener() {
			@Override
			public void transportInterupted() {
				Logger.warn("Connection with Data Hub (" + config.url() + ") interrupted!");
				connected.set(false);
			}

			@Override
			public void transportResumed() {
				Logger.info("Connection with Data Hub (" + config.url() + ") established!");
				connected.set(true);
				recoverConsumers();
			}
		};
	}

	private void clearProducers() {
		producers.values().forEach(JmsProducer::close);
		producers.clear();
	}

	private void clearConsumers() {
		consumers.values().forEach(JmsConsumer::close);
		consumers.clear();
	}

	private JmsConsumer topicConsumer(String path, String messageSelector) {
		try {
			return new TopicConsumer(session, path, messageSelector);
		} catch (JMSException e) {
			Logger.error(e);
			return null;
		}
	}

	private JmsConsumer durableTopicConsumer(String path, String subscriberId, String messageSelector) {
		try {
			return new DurableTopicConsumer(session, path, messageSelector, subscriberId);
		} catch (JMSException e) {
			Logger.error(e);
			return null;
		}
	}

	private QueueConsumer queueConsumer(String path) {
		try {
			return new QueueConsumer(session, path);
		} catch (JMSException e) {
			Logger.error(e);
			return null;
		}
	}

	private void recoverConsumers() {
		if (!started.get()) return;
		if (!eventConsumers.isEmpty() && consumers.isEmpty())
			for (String path : eventConsumers.keySet()) consumers.put(path, topicConsumer(path, path));
		if (!messageConsumers.isEmpty() && consumers.isEmpty())
			for (String path : messageConsumers.keySet()) {
				if (!consumers.containsKey(path) && session != null) consumers.put(path, queueConsumer(path));
				for (MessageConsumer mConsumer : messageConsumers.get(path)) {
					Consumer<jakarta.jms.Message> messageConsumer = m -> mConsumer.accept(textFrom(m), callback(m));
					jmsMessageConsumers.put(mConsumer, messageConsumer.hashCode());
					consumers.get(path).listen(messageConsumer);
				}
			}
	}

	private synchronized void recoverEventsAndMessages() {
		recoverEvents();
		recoverMessages();
	}

	private void recoverEvents() {
		if (eventOutBox == null) return;
		synchronized (eventOutBox) {
			if (eventOutBox.isEmpty()) return;
			Logger.info("Recovering events...");
			while (!eventOutBox.isEmpty())
				for (Map.Entry<String, Event> event : eventOutBox.get())
					if (doSendEvent(event.getKey(), event.getValue())) eventOutBox.pop();
					else return;
		}
		Logger.info("All events recovered!");
	}

	private void recoverMessages() {
		if (messageOutBox == null) return;
		synchronized (messageOutBox) {
			if (!messageOutBox.isEmpty())
				while (!messageOutBox.isEmpty()) {
					Map.Entry<String, String> message = messageOutBox.get();
					if (message == null) continue;
					if (doSendMessageToQueue(message.getKey(), message.getValue())) messageOutBox.pop();
					else break;
				}
		}
	}

	private void checkConnection() {
		if (session != null && config.url().startsWith("failover") && !connected.get()) {
			Logger.debug("Data-hub currently disconnected. Waiting for reconnection...");
			return;
		}
		if (connection != null && ((ActiveMQConnection) connection).isStarted() && session != null && ((ActiveMQSession) session).isRunning()) {
			connected.set(true);
			return;
		}
		Logger.debug("Restarting data-hub connection...");
		stop();
		try {
			connect();
		} catch (JMSException ignored) {
		}
		connected.set(true);
	}

	private void initConnection() {
		try {
			connection = BrokerConnector.createConnection(config, connectionListener());
			if (connection != null) {
				if (config.clientId() != null && !config.clientId().isEmpty())
					connection.setClientID(config.clientId());
				connection.start();
			}
		} catch (JMSException e) {
			Logger.error(e);
		}
	}

	private String callback(jakarta.jms.Message m) {
		try {
			ActiveMQDestination replyTo = (ActiveMQDestination) m.getJMSReplyTo();
			return replyTo == null ? null : replyTo.getPhysicalName();
		} catch (JMSException e) {
			return null;
		}
	}

	public static String createRandomString() {
		Random random = new Random(System.currentTimeMillis());
		long randomLong = random.nextLong();
		return Long.toHexString(randomLong);
	}

	public static class NamedThreadFactory implements ThreadFactory {
		private final AtomicInteger sequence = new AtomicInteger(1);
		private final String prefix;

		public NamedThreadFactory(String prefix) {
			this.prefix = prefix;
		}

		public Thread newThread(Runnable r) {
			Thread thread = new Thread(r);
			int seq = this.sequence.getAndIncrement();
			thread.setName(this.prefix + (seq > 1 ? "-" + seq : ""));
			if (!thread.isDaemon()) {
				thread.setDaemon(true);
			}
			return thread;
		}
	}
}
