package io.intino.datahub.service.jms;


import io.intino.alexandria.jms.MessageReader;
import io.intino.alexandria.jms.QueueProducer;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.DataHub;
import io.intino.datahub.broker.BrokerManager;
import io.intino.datahub.broker.jms.MessageTranslator;
import org.apache.activemq.command.ActiveMQTempQueue;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;

public class NessService {

	public NessService(DataHub hub) {
		BrokerManager manager = hub.brokerService().manager();
		manager.registerQueueConsumer("service.ness.seal", m -> response(manager, m, new SealRequest(hub).accept(MessageReader.textFrom(m))));
		manager.registerQueueConsumer("service.ness.backup", m -> response(manager, m, new BackupRequest(hub).accept(MessageReader.textFrom(m))));
	}

	private void response(BrokerManager manager, Message requestMessage, String response) {
		new Thread(() -> {
			try {
				Destination reply = requestMessage.getJMSReplyTo();
				QueueProducer queueProducer = manager.queueProducerOf(reply instanceof ActiveMQTempQueue ? ((ActiveMQTempQueue) reply).getQueueName() : reply.toString());
				Message message = MessageTranslator.toJmsMessage(response);
				if (message == null) return;
				message.setJMSCorrelationID(requestMessage.getJMSCorrelationID());
				queueProducer.produce(message);
			} catch (JMSException e) {
				Logger.error(e);
			}
		}).start();
	}
}