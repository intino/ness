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
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import org.apache.activemq.Closeable;
import org.apache.activemq.command.ActiveMQTempQueue;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static io.intino.datahub.broker.jms.JmsMessageTranslator.toJmsMessage;

public class NessService {
	public static final String SERVICE_NESS_DATAMARTS = "service.ness.datamarts";
	public static final String SERVICE_NESS_DATAMARTS_NOTIFICATIONS = "service.ness.datamarts.notifications";
	private BrokerManager manager;
	private ExecutorService dispatcherService;
	private DatamartNotifier datamartNotifier;
	private final DataHubBox box;

	public NessService(DataHubBox box) {
		this.box = box;
	}

	public void start() {
		dispatcherService = Executors.newSingleThreadExecutor(r -> new Thread(r, "Ness Datamarts Service"));
		manager = box.brokerService().manager();
		datamartNotifier = new DatamartNotifier(5, TimeUnit.SECONDS);
		manager.registerQueueConsumer("service.ness.seal", m -> response(manager, m, new SealRequest(box).accept(MessageReader.textFrom(m))));
		manager.registerQueueConsumer("service.ness.seal.last", m -> response(manager, m, new LastSealRequest(box).accept(MessageReader.textFrom(m))));
		manager.registerQueueConsumer("service.ness.backup", m -> response(manager, m, new BackupRequest(box).accept(MessageReader.textFrom(m))));
		manager.registerQueueConsumer("service.ness.datalake", m -> response(manager, m, new DatalakeRequest(box).accept(m)));
		manager.registerQueueConsumer("service.ness.metamodel", m -> response(manager, m, new MetamodelRequest(box).accept(m)));
		manager.registerQueueConsumer(SERVICE_NESS_DATAMARTS, m -> response(manager, m, new DatamartsRequest(box).accept(m)));
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			datamartNotifier.close();
			shutdown(dispatcherService);
		}));
	}

	public void notifyDatamartReload(String datamartName) {
		TopicProducer topicProducer = box.brokerService().manager().topicProducerOf("service.ness.datamarts");
		topicProducer.produce(toJmsMessage("{\"operation\":\"reload\", datamart:\"" + datamartName + "\"}"));
		topicProducer.close();
	}

	public void notifyDatamartChange(Stream<String> sourcesChanged) {
		datamartNotifier.notify(sourcesChanged);
	}

	private void response(BrokerManager manager, Message requestMessage, String response) {
		response(manager, requestMessage, JmsMessageTranslator.toJmsMessage(response));
	}

	private void response(BrokerManager manager, Message request, Message response) {
		dispatcherService.execute(() -> {
			try {
				if (response == null) return;
				QueueProducer queueProducer = producer(manager, request);
				if (queueProducer == null) return;
				response.setJMSCorrelationID(request.getJMSCorrelationID());
				queueProducer.produce(response);
			} catch (Throwable e) {
				Logger.error("Error while handling response: " + e.getMessage(), e);
			}
		});
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

	private static void shutdown(ExecutorService executorService) {
		try {
			if (executorService == null || executorService.isShutdown()) return;
			executorService.shutdown();
			executorService.awaitTermination(1, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			Logger.error(e);
		}
	}

	private class DatamartNotifier implements Closeable {

		private final TopicProducer topicProducer;
		private final ScheduledExecutorService thread;
		private volatile Set<String> sourcesChanged = new HashSet<>();

		public DatamartNotifier(int timeAmount, TimeUnit timeUnit) {
			this.topicProducer = manager.topicProducerOf(SERVICE_NESS_DATAMARTS_NOTIFICATIONS);
			this.thread = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "DatamartNotifier"));
			this.thread.scheduleAtFixedRate(this::send, timeAmount, timeAmount, timeUnit);
		}

		public void notify(Stream<String> sources) {
			if (sources == null) return;
			synchronized (this) {
				sources.forEach(s -> sourcesChanged.add(s));
			}
		}

		private void send() {
			try {
				Set<String> sources = swap();
				JsonObject notification = createNotificationWith(sources);
				topicProducer.produce(MessageWriter.write(Json.toJson(notification)));
			} catch (Throwable e) {
				Logger.error("Error while sending datamart notifications: " + e.getMessage(), e);
			}
		}

		private static JsonObject createNotificationWith(Set<String> sources) {
			JsonObject notification = new JsonObject();
			notification.addProperty("operation", "refresh");
			JsonArray jsonElements = new JsonArray();
			sources.forEach(jsonElements::add);
			notification.add("changes", jsonElements);
			return notification;
		}

		private Set<String> swap() {
			synchronized (this) {
				Set<String> sources = this.sourcesChanged;
				this.sourcesChanged = new HashSet<>();
				return sources;
			}
		}

		@Override
		public void close() {
			shutdown(thread);
		}
	}
}