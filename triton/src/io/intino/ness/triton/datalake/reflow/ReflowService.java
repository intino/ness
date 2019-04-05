package io.intino.ness.triton.datalake.reflow;

import com.google.gson.Gson;
import io.intino.alexandria.jms.Consumer;
import io.intino.alexandria.jms.QueueProducer;
import io.intino.alexandria.logger.Logger;
import io.intino.ness.datalake.file.FileDatalake;
import io.intino.ness.triton.box.TritonBox;
import io.intino.ness.triton.box.actions.PauseTankAction;
import io.intino.ness.triton.box.actions.ResumeTankAction;
import io.intino.ness.triton.graph.Datalake;
import org.apache.activemq.command.ActiveMQDestination;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static io.intino.alexandria.jms.Consumer.textFrom;
import static io.intino.alexandria.jms.MessageFactory.createMessageFor;

public class ReflowService implements Consumer {
	private final TritonBox box;
	private ReflowProcess handler;
	private int blockSize;
	private Session session;
	private List<Datalake.Tank> pausedTanks;

	public ReflowService(TritonBox box) {
		this.box = box;
	}

	public void consume(Message message) {
		String text = textFrom(message);
		if (text.contains("blockSize")) startReflow(text);
		else if (text.contains("quickReflow")) quickReflowInfo(message);
		else if (text.contains("startQuickReflow")) startQuickReflow(message);
		else if (text.contains("ready") && this.handler != null) ready(message);
		else if (!text.contains("ready") && handler != null) next();
		else if (text.contains("finish")) finish();
	}

	private void startQuickReflow(Message message) {
		pauseTanks();
		box.sessionManager().seal();
		replyTo(message, "ack");
	}

	private void startReflow(String text) {
		if (this.handler != null) return;
		ReflowConfiguration reflow = new Gson().fromJson(text, ReflowConfiguration.class);
		this.blockSize = reflow.blockSize();
		createSession(reflow);
	}

	private void quickReflowInfo(Message message) {
		try {
			if (box.datalake() instanceof FileDatalake)
				replyTo(message, "file://" + new File(((FileDatalake) box.datalake()).root().getAbsolutePath() + "/events").getCanonicalPath());
		} catch (IOException e) {
			Logger.error(e.getMessage(), e);
		}
	}

	private void createSession(ReflowConfiguration configuration) {
		Logger.info("Shutting down actual session");
		box.restartBus(false);
		pauseTanks();
		this.session = box.busManager().transactedSession();
		box.sessionManager().seal();
		this.handler = new ReflowProcess(session, box.datalake(), box.scale(), configuration);
		Logger.info("Reflow session created");
	}

	private void ready(final Message message) {
		new Thread(() -> {
			try {
				if (session != null) {
					final QueueProducer producer = new QueueProducer(session, message.getJMSReplyTo());
					producer.produce(createMessageFor("ack"));
					producer.close();
					session.commit();
				}
				Logger.info("Ready to reflow");
			} catch (JMSException e) {
				Logger.error(e.getMessage(), e);
			}
		}).start();
	}

	private void next() {
		Logger.info("sending next block of " + blockSize + " messages");
		handler.next();
	}

	private void finish() {
		resumeTanks();
		this.handler = null;
		Logger.info("Reflow session finished");
//		if(box.busService().isPersistent()) box.restartBus(true); TODO falla al arrancar con persistencia
	}

	private void resumeTanks() {
		pausedTanks.forEach(t -> new ResumeTankAction(box, t.name()).execute());
	}

	private void pauseTanks() {
		this.pausedTanks = box.graph().datalake().tankList().stream().filter(Datalake.Tank::active).collect(Collectors.toList());
		this.pausedTanks.forEach(t -> new PauseTankAction(box, t.name()).execute());
	}

	private void replyTo(Message request, String reply) {
		try {
			box.busManager().getQueueProducer(((ActiveMQDestination) request.getJMSReplyTo()).getPhysicalName()).produce(createMessageFor(reply));
		} catch (JMSException e) {
			Logger.error(e.getMessage(), e);
		}
	}
}