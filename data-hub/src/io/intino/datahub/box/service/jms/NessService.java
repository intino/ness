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

public class NessService {

	public NessService(DataHubBox box) {
		BrokerManager manager = box.brokerService().manager();
		manager.registerQueueConsumer("service.ness.seal", m -> response(manager, m, new SealRequest(box).accept(MessageReader.textFrom(m))));
		manager.registerQueueConsumer("service.ness.seal.last", m -> response(manager, m, new LastSealRequest(box).accept(MessageReader.textFrom(m))));
		manager.registerQueueConsumer("service.ness.backup", m -> response(manager, m, new BackupRequest(box).accept(MessageReader.textFrom(m))));
		manager.registerQueueConsumer("service.ness.datalake", m -> response(manager, m, new DatalakeRequest(box).accept(m)));
	}

	private void response(BrokerManager manager, Message requestMessage, String response) {
		response(manager, requestMessage, MessageTranslator.toJmsMessage(response));
	}

	private void response(BrokerManager manager, Message request, Message response) {
		new Thread(() -> {
			try {
				Destination reply = request.getJMSReplyTo();
				QueueProducer queueProducer = manager.queueProducerOf(reply instanceof ActiveMQTempQueue ? ((ActiveMQTempQueue) reply).getQueueName() : reply.toString());
				if (response == null) return;
				response.setJMSCorrelationID(request.getJMSCorrelationID());
				queueProducer.produce(response);
			} catch (JMSException e) {
				Logger.error(e);
			}
		}).start();
	}
}