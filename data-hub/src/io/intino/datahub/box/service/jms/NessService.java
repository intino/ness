package io.intino.datahub.box.service.jms;


import io.intino.alexandria.jms.MessageReader;
import io.intino.alexandria.jms.QueueProducer;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.broker.BrokerManager;
import io.intino.datahub.broker.jms.MessageTranslator;
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
		manager.registerQueueConsumer("service.ness.datalake.eventstore", m -> response(manager, m, new EventStoreRequest(box).accept(m)));
		manager.registerQueueConsumer("service.ness.datalake.entitystore", m -> response(manager, m, new EntityStoreRequest(box).accept(m)));
	}

	private void response(BrokerManager manager, Message requestMessage, String response) {
		response(manager, requestMessage, MessageTranslator.toJmsMessage(response));
	}

	private void response(BrokerManager manager, Message request, Message response) {
		new Thread(() -> {
			try {
				QueueProducer queueProducer = producer(manager, request);
				if (response == null) return;
				response.setJMSCorrelationID(request.getJMSCorrelationID());
				queueProducer.produce(response);
			} catch (JMSException e) {
				Logger.error(e);
			}
		}).start();
	}

	private void response(BrokerManager manager, Message request, Stream<Message> response) {
		QueueProducer producer = producer(manager, request);
		if (producer == null) return;
		new Thread(() -> response.forEach(m -> {
			try {
				m.setJMSCorrelationID(request.getJMSCorrelationID());
				producer.produce(m);
			} catch (JMSException e) {
				Logger.error(e);
			}
		})).start();
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