package io.intino.ness.datalake;

import io.intino.alexandria.jms.Consumer;
import io.intino.alexandria.logger.Logger;
import io.intino.ness.box.NessServiceBox;
import io.intino.ness.graph.Datalake.Tank;
import io.intino.ness.graph.NessGraph;
import io.intino.ness.graph.User;
import org.apache.activemq.command.ActiveMQDestination;

import javax.jms.JMSException;
import javax.jms.Message;

import static io.intino.alexandria.jms.MessageFactory.createMessageFor;
import static java.util.stream.Collectors.joining;

public class AdminService implements Consumer {
	private final NessServiceBox box;

	public AdminService(NessServiceBox box) {
		this.box = box;
	}

	@Override
	public void consume(Message message) {
		String text = Consumer.textFrom(message);
		NessGraph nessGraph = box.graph();
		if (text.startsWith("users")) replyTo(message, nessGraph.userList().stream().map(User::name).collect(joining(",")));
		if (text.startsWith("seal")) {
			box.datalake().seal();
			replyTo(message, "sealed");
		} else if (text.startsWith("tanks"))
			replyTo(message, nessGraph.datalake().tankList().stream().map(Tank::name).collect(joining(";")));
	}

	private void replyTo(Message request, String reply) {
		try {
			box.busManager().getQueueProducer(((ActiveMQDestination) request.getJMSReplyTo()).getPhysicalName()).produce(createMessageFor(reply));
		} catch (JMSException e) {
			Logger.error(e.getMessage(), e);
		}
	}
}
