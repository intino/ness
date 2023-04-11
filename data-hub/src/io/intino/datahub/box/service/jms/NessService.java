package io.intino.datahub.box.service.jms;


import io.intino.alexandria.jms.MessageReader;
import io.intino.alexandria.jms.QueueProducer;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.broker.BrokerManager;
import io.intino.datahub.broker.jms.JmsMessageTranslator;
import org.apache.activemq.command.ActiveMQTempQueue;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import java.util.stream.Stream;

public class NessService {

	public NessService(DataHubBox box) {
		BrokerManager manager = box.brokerService().manager();
		manager.registerQueueConsumer("service.ness.seal", m -> response(manager, m, new SealRequest(box).accept(MessageReader.textFrom(m))));
		manager.registerQueueConsumer("service.ness.seal.last", m -> response(manager, m, new LastSealRequest(box).accept(MessageReader.textFrom(m))));
		manager.registerQueueConsumer("service.ness.backup", m -> response(manager, m, new BackupRequest(box).accept(MessageReader.textFrom(m))));
		manager.registerQueueConsumer("service.ness.datalake", m -> response(manager, m, new DatalakeRequest(box).accept(m)));
		manager.registerQueueConsumer("service.ness.datamarts", m -> response(manager, m, new DatalakeRequest(box).accept(m)));
	}

	private void response(BrokerManager manager, Message requestMessage, String response) {
		response(manager, requestMessage, JmsMessageTranslator.toJmsMessage(response));
	}

	private void response(BrokerManager manager, Message request, Message response) {
		new Thread(() -> {
			try {
				if (response == null) return;
				QueueProducer queueProducer = producer(manager, request);
				if(queueProducer == null) return;
				response.setJMSCorrelationID(request.getJMSCorrelationID());
				queueProducer.produce(response);
			} catch (Throwable e) {
				Logger.error("Error while handling response: " + e.getMessage(), e);
			}
		}).start();
	}

	private void response(BrokerManager manager, Message request, Stream<Message> response) {
		if(response == null) return;
		QueueProducer producer = producer(manager, request);
		if (producer == null) return;
		new Thread(() -> handleResponse(request, response, producer)).start();
	}

	private void handleResponse(Message request, Stream<Message> response, QueueProducer producer) {
		response.forEach(m -> {
			try {
				m.setJMSCorrelationID(request.getJMSCorrelationID());
				producer.produce(m);
			} catch (Throwable e) {
				Logger.error("Error while handling response: " + e.getMessage(), e);
			}
		});
	}

	private static QueueProducer producer(BrokerManager manager, Message request) {
		try {
			Destination reply = request.getJMSReplyTo();
			return manager.queueProducerOf(reply instanceof ActiveMQTempQueue ? ((ActiveMQTempQueue) reply).getQueueName() : reply.toString());
		} catch (JMSException e) {
			Logger.error(e);
			return null;
		}
	}
}