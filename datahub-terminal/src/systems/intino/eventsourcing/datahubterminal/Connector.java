package systems.intino.eventsourcing.datahubterminal;

import jakarta.jms.Message;
import systems.intino.eventsourcing.event.Event;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface Connector {

	String clientId();

	void sendEvent(String path, Event event);

	void sendEvents(String path, List<Event> events);

	void sendEvent(String path, Event event, int expirationInSeconds);

	void sendEvents(String path, List<Event> events, int expirationInSeconds);

	@Deprecated
	default void sendMessage(String path, String message) {
		sendQueueMessage(path, message);
	}

	void sendQueueMessage(String path, String message);

	void sendTopicMessage(String path, String message);

	void attachListener(String path, Consumer<Event> onEventReceived);

	void attachListener(String path, Consumer<Event> onEventReceived, String messageSelector);

	void attachListener(String path, String subscriberId, Consumer<Event> onEventReceived);

	default void attachListener(String path, String subscriberId, Consumer<Event> onEventReceived, Predicate<Instant> filter) {
		attachListener(path, subscriberId, onEventReceived, filter, null);
	}

	void attachListener(String path, String subscriberId, Consumer<Event> onEventReceived, Predicate<Instant> filter, String messageSelector);

	void attachListener(String path, String subscriberId, Consumer<Event> onEventReceived, String messageSelector);

	void attachListener(String path, MessageConsumer consumer);

	void attachListener(String path, String subscriberId, MessageConsumer consumer);

	void detachListeners(Consumer<Event> consumer);

	void detachListeners(MessageConsumer consumer);

	void detachListeners(String path);

	void createSubscription(String path, String subscriberId);

	void destroySubscription(String subscriberId);

	Message requestResponse(String path, Message message);

	Message requestResponse(String path, Message message, long timeout, TimeUnit timeUnit);

	void requestResponse(String path, Message message, String responsePath);

	long defaultTimeoutAmount();

	TimeUnit defaultTimeoutUnit();

	interface MessageConsumer {
		void accept(String message, String callback);
	}
}