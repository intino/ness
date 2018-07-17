package io.intino.ness.datalake;

import io.intino.konos.jms.Consumer;
import io.intino.ness.box.NessBox;
import io.intino.ness.graph.Tank;
import org.apache.activemq.command.ActiveMQDestination;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.stream.Collectors;

import static io.intino.konos.jms.MessageFactory.createMessageFor;

public class AdminSession implements Consumer {
	private final NessBox box;

	public AdminSession(NessBox box) {
		this.box = box;
	}

	@Override
	public void consume(Message message) {
		String text = Consumer.textFrom(message);
		if (text.contains("tanks"))
			replyTo(message, String.join(";", box.nessGraph().tankList().stream().map(Tank::qualifiedName).collect(Collectors.toList())));
	}


	private void replyTo(Message request, String reply) {
		try {
			box.busManager().getQueueProducer(((ActiveMQDestination) request.getJMSReplyTo()).getPhysicalName()).produce(createMessageFor(reply));
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);

		}
	}
}
