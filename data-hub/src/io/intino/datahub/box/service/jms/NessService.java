package io.intino.datahub.box.service.jms;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.intino.alexandria.Json;
import io.intino.alexandria.jms.MessageReader;
import io.intino.alexandria.jms.MessageWriter;
import io.intino.alexandria.jms.QueueProducer;
import io.intino.alexandria.jms.TopicProducer;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.broker.BrokerManager;
import io.intino.datahub.broker.jms.JmsMessageTranslator;
import org.apache.activemq.command.ActiveMQTempQueue;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class NessService {
	public static final String SERVICE_NESS_DATAMARTS = "service.ness.datamarts";
	public static final String SERVICE_NESS_DATAMARTS_NOTIFICATIONS = "service.ness.datamarts.notifications";
	private final BrokerManager manager;
	private final ExecutorService dispatcherService;
	private final TopicProducer notifier;

	public NessService(DataHubBox box) {
		dispatcherService = Executors.newFixedThreadPool(16, r -> new Thread(r, "Ness Datamarts Service"));
		manager = box.brokerService().manager();
		notifier = manager.topicProducerOf(SERVICE_NESS_DATAMARTS_NOTIFICATIONS);
		manager.registerQueueConsumer("service.ness.seal", m -> response(manager, m, new SealRequest(box).accept(MessageReader.textFrom(m))));
		manager.registerQueueConsumer("service.ness.seal.last", m -> response(manager, m, new LastSealRequest(box).accept(MessageReader.textFrom(m))));
		manager.registerQueueConsumer("service.ness.backup", m -> response(manager, m, new BackupRequest(box).accept(MessageReader.textFrom(m))));
		manager.registerQueueConsumer("service.ness.datalake", m -> response(manager, m, new DatalakeRequest(box).accept(m)));
		manager.registerQueueConsumer(SERVICE_NESS_DATAMARTS, m -> response(manager, m, new DatamartsRequest(box).accept(m)));
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				dispatcherService.shutdown();
				dispatcherService.awaitTermination(1, TimeUnit.MINUTES);
			} catch (InterruptedException e) {
				Logger.error(e);
			}
		}));
	}

	public void notifyDatamartChange(List<String> sourcesChanged) {
		JsonObject notification = new JsonObject();
		notification.addProperty("operation", "refresh");
		JsonArray jsonElements = new JsonArray();
		sourcesChanged.forEach(jsonElements::add);
		notification.add("changes", jsonElements);
		try {
			notifier.produce(MessageWriter.write(Json.toJson(notification)));
		} catch (JMSException e) {
			Logger.error(e);
		}

	}

	private void response(BrokerManager manager, Message requestMessage, String response) {
		response(manager, requestMessage, JmsMessageTranslator.toJmsMessage(response));
	}

	private void response(BrokerManager manager, Message request, Message response) {
		new Thread(() -> {
			try {
				if (response == null) return;
				QueueProducer queueProducer = producer(manager, request);
				if (queueProducer == null) return;
				response.setJMSCorrelationID(request.getJMSCorrelationID());
				queueProducer.produce(response);
			} catch (Throwable e) {
				Logger.error("Error while handling response: " + e.getMessage(), e);
			}
		}).start();
	}

	private void response(BrokerManager manager, Message request, Stream<Message> response) {
		dispatcherService.execute(() -> {
			if (response == null) return;
			QueueProducer producer = producer(manager, request);
			if (producer == null) return;
			publishResponse(request, response, producer);
		});
	}

	private void publishResponse(Message request, Stream<Message> response, QueueProducer producer) {
		response.forEach(m -> {
			try {
				m.setJMSCorrelationID(request.getJMSCorrelationID());
				producer.produce(m);
			} catch (Throwable e) {
				Logger.error("Error while handling response: " + e.getMessage(), e);
			}
		});
		try {
			manager.unregisterQueueProducer(replyQueue(request));
		} catch (JMSException e) {
			Logger.error(e);
		}
	}

	private static QueueProducer producer(BrokerManager manager, Message request) {
		try {
			String queue = replyQueue(request);
			return manager.queueProducerOf(queue);
		} catch (JMSException e) {
			Logger.error(e);
			return null;
		}
	}

	private static String replyQueue(Message request) throws JMSException {
		Destination reply = request.getJMSReplyTo();
		return reply instanceof ActiveMQTempQueue r ? r.getQueueName() : reply.toString();
	}
}