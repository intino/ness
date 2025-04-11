package systems.intino.eventsourcing.datahubterminal;

import io.intino.alexandria.Json;
import io.intino.alexandria.Resource;
import io.intino.alexandria.logger.Logger;
import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;
import jakarta.jms.TextMessage;
import org.apache.activemq.command.ActiveMQBytesMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import systems.intino.eventsourcing.event.Event;
import systems.intino.eventsourcing.event.Event.Format;
import systems.intino.eventsourcing.event.message.MessageEvent;
import systems.intino.eventsourcing.event.resource.ResourceEvent;
import systems.intino.eventsourcing.message.MessageReader;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static systems.intino.eventsourcing.jms.MessageReader.textFrom;

class MessageTranslator {
	public static jakarta.jms.Message serialize(Event event) throws IOException, JMSException {
		if (event instanceof ResourceEvent) return serializeAsResource((ResourceEvent) event);
		return serialize(event.ts().toString(), event.type(), event.ss(), event.toString());
	}

	private static jakarta.jms.Message serialize(String ts, String type, String ss, String payload) throws JMSException {
		TextMessage textMessage = new ActiveMQTextMessage();
		if (ts != null) textMessage.setStringProperty("ts", ts);
		if (ss != null && !ss.isEmpty()) textMessage.setStringProperty("ss", ss);
		if (type != null && !type.isEmpty()) textMessage.setStringProperty("type", type);
		textMessage.setStringProperty("format", Format.Message.name());
		textMessage.setText(payload);
		return textMessage;
	}

	public static jakarta.jms.Message serialize(List<Event> events) throws IOException, JMSException {
		String ss = events.stream().map(Event::ss).distinct().collect(Collectors.joining(";"));
		String ts = events.stream().map(Event::ts).map(Objects::toString).collect(Collectors.joining(";"));
		String types = events.stream().map(Event::type).distinct().collect(Collectors.joining(";"));
		String content = events.stream().map(Event::toString).collect(Collectors.joining("\n\n"));
		return serialize(ts, types, ss, content);
	}

	static Stream<Event> deserialize(jakarta.jms.Message message) {
		try {
			String format = message.getStringProperty("format");
			if (format != null && format.equals(Format.Resource.name()))
				return Stream.of(deserializeAsResource(message));
			else return stream(new MessageReader(textFrom(message))).map(MessageEvent::new);
		} catch (JMSException | IOException e) {
			Logger.error(e);
			return Stream.empty();
		}
	}

	private static jakarta.jms.Message serializeAsResource(ResourceEvent event) throws JMSException, IOException {
		BytesMessage message = new ActiveMQBytesMessage();
		if (event.ts() != null) message.setLongProperty("ts", event.ts().toEpochMilli());
		if (event.ss() != null && !event.ss().isEmpty()) message.setStringProperty("ss", event.ss());
		if (event.type() != null && !event.type().isEmpty()) message.setStringProperty("type", event.type());
		byte[] bytes = event.resource().bytes();
		message.setStringProperty("resource.name", event.resource().name());
		message.setStringProperty("resource.metadata", Json.toJson(event.resource().metadata()));
		message.setIntProperty("resource.data.length", bytes.length);
		message.setStringProperty("format", Format.Resource.name());
		message.writeBytes(bytes);
		return message;
	}


	public static ResourceEvent deserializeAsResource(jakarta.jms.Message message) throws JMSException, IOException {
		if (!(message instanceof BytesMessage)) return null;
		BytesMessage m = (BytesMessage) message;
		String resourceName = m.getStringProperty("resource.name");
		Resource.Metadata metadata = Json.fromJson(m.getStringProperty("resource.metadata"), Resource.Metadata.class);
		int dataLength = m.getIntProperty("resource.data.length");
		byte[] data = new byte[dataLength];
		m.readBytes(data);
		Resource resource = new Resource(resourceName, data);
		resource.metadata().putAll(metadata.properties());
		String type = m.getStringProperty("type");
		String ss = m.getStringProperty("ss");
		Instant ts = Instant.ofEpochMilli(m.getLongProperty("ts"));
		return new ResourceEvent(type, ss, resource).ts(ts);
	}

	private static <T> Stream<T> stream(Iterable<T> it) {
		return StreamSupport.stream(it.spliterator(), false);
	}
}
