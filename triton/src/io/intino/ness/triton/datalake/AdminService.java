package io.intino.ness.triton.datalake;

import io.intino.alexandria.jms.Consumer;
import io.intino.alexandria.logger.Logger;
import io.intino.ness.triton.box.TritonBox;
import io.intino.ness.triton.box.actions.SealAction;
import io.intino.ness.triton.graph.Tank;
import io.intino.ness.triton.graph.TritonGraph;
import io.intino.ness.triton.graph.User;
import org.apache.activemq.command.ActiveMQDestination;

import javax.jms.JMSException;
import javax.jms.Message;

import static io.intino.alexandria.jms.MessageFactory.createMessageFor;
import static java.util.stream.Collectors.joining;

public class AdminService implements Consumer {
	private final TritonBox box;

	public AdminService(TritonBox box) {
		this.box = box;
	}

	@Override
	public void consume(Message message) {
		String text = Consumer.textFrom(message);
		TritonGraph tritonGraph = box.graph();
		if (text.startsWith("users")) replyTo(message, tritonGraph.userList().stream().map(User::name).collect(joining(",")));
		if (text.startsWith("seal")) {
			new SealAction(box).execute();
			replyTo(message, "sealed");
		} else if (text.startsWith("tanks"))
			replyTo(message, tritonGraph.tankList().stream().map(Tank::name).collect(joining(";")));
	}

	private void replyTo(Message request, String reply) {
		try {
			box.busManager().getQueueProducer(((ActiveMQDestination) request.getJMSReplyTo()).getPhysicalName()).produce(createMessageFor(reply));
		} catch (JMSException e) {
			Logger.error(e.getMessage(), e);
		}
	}
}
