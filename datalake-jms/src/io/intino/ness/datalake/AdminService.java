package io.intino.ness.datalake;

import io.intino.konos.jms.Consumer;
import io.intino.ness.box.NessBox;
import io.intino.ness.graph.Tank;
import org.apache.activemq.command.ActiveMQDestination;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.stream.Collectors;

import static io.intino.konos.jms.MessageFactory.createMessageFor;

public class AdminService implements Consumer {
	private final NessBox box;

	public AdminService(NessBox box) {
		this.box = box;
	}

	@Override
	public void consume(Message message) {
		String text = Consumer.textFrom(message);
		if (text.startsWith("tanks"))
			replyTo(message, String.join(";", box.nessGraph().tankList().stream().map(Tank::qualifiedName).collect(Collectors.toList())));
		else if (text.startsWith("batch")) {
			final String[] split = text.split(":");
			final io.intino.ness.datalake.graph.Tank tank = box.datalake().tank(split[1]);
			tank.batch(Integer.parseInt(split[2]));
		} else if (text.startsWith("endbatch")) {
			final String[] split = text.split(":");
			final io.intino.ness.datalake.graph.Tank tank = box.datalake().tank(split[1]);
			tank.endBatch();
		}
	}

	private void replyTo(Message request, String reply) {
		try {
			box.busManager().getQueueProducer(((ActiveMQDestination) request.getJMSReplyTo()).getPhysicalName()).produce(createMessageFor(reply));
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
		}
	}
}
