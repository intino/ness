package systems.intino.eventsourcing.datahubterminal.remotedatalake;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.intino.alexandria.logger.Logger;
import jakarta.jms.*;
import systems.intino.eventsourcing.datahubterminal.JmsConnector;
import systems.intino.eventsourcing.jms.JmsProducer;
import systems.intino.eventsourcing.jms.QueueProducer;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static systems.intino.eventsourcing.datahubterminal.JmsConnector.createRandomString;

public class DatalakeAccessor {

	public static final String PATH = "service.ness.datalake";
	private final JmsConnector connector;
	private final Session session;

	DatalakeAccessor(JmsConnector connector) {
		this.connector = connector;
		session = connector.session();
	}

	public jakarta.jms.MessageConsumer queryWithConsumer(String query) {
		Session session = connector.session();
		try {
			TemporaryQueue temporaryQueue = session.createTemporaryQueue();
			jakarta.jms.MessageConsumer consumer = session.createConsumer(temporaryQueue);
			sendRequest(query, temporaryQueue);
			return consumer;
		} catch (JMSException e) {
			Logger.error(e);
			return null;
		}
	}

	public Message query(String query) {
		AtomicReference<Message> response = new AtomicReference<>(null);
		Session session = connector.session();
		try {
			TemporaryQueue temporaryQueue = session.createTemporaryQueue();
			jakarta.jms.MessageConsumer consumer = session.createConsumer(temporaryQueue);
			final Object monitor = new Object();
			consumer.setMessageListener(m -> {
				response.set(m);
				synchronized (monitor) {
					monitor.notify();
				}
			});
			sendRequest(query, temporaryQueue);
			waitForResponse(monitor);
			consumer.close();
			return response.get();
		} catch (JMSException e) {
			Logger.error(e);
		}
		return null;
	}

	private static void waitForResponse(Object monitor) {
		try {
			synchronized (monitor) {
				monitor.wait(1000 * 30);
			}
		} catch (InterruptedException e) {
			Logger.error(e);
		}
	}

	private void sendMessage(JmsProducer producer, Message message, int expirationTimeInSeconds) {
		try {
			Thread thread = new Thread(() -> producer.produce(message, expirationTimeInSeconds));
			thread.start();
			thread.join(1000);
			thread.interrupt();
		} catch (InterruptedException ignored) {
		}
	}

	private void sendRequest(String query, TemporaryQueue temporaryQueue) throws JMSException {
		QueueProducer producer = new QueueProducer(session, PATH);
		final TextMessage txtMessage = session.createTextMessage();
		txtMessage.setText(query);
		txtMessage.setJMSReplyTo(temporaryQueue);
		txtMessage.setJMSCorrelationID(createRandomString());
		sendMessage(producer, txtMessage, 100);
		producer.close();
	}

	public static JsonObject reflowSchema(String tank, String source, List<String> tubs) {
		JsonArray tubsArray = new JsonArray();
		tubs.forEach(tubsArray::add);
		return reflowSchema(tank, source, tubsArray);
	}

	public static JsonObject reflowSchema(String tank, String source, JsonArray tubs) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("operation", "reflow");
		jsonObject.addProperty("tank", tank);
		jsonObject.addProperty("source", source);
		jsonObject.add("tubs", tubs);
		return jsonObject;
	}
}
