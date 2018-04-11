package io.intino.ness.datalake.reflow;

import com.google.gson.Gson;
import io.intino.konos.jms.Consumer;
import io.intino.konos.jms.QueueProducer;
import io.intino.ness.box.NessBox;
import io.intino.ness.box.actions.PauseTankAction;
import io.intino.ness.box.actions.ResumeTankAction;
import io.intino.ness.box.actions.SortTankAction;
import io.intino.ness.box.schemas.Reflow;
import io.intino.ness.datalake.graph.AbstractTank;
import io.intino.ness.datalake.graph.Tank;
import org.apache.activemq.command.ActiveMQDestination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static io.intino.konos.jms.Consumer.textFrom;
import static io.intino.konos.jms.MessageFactory.createMessageFor;
import static io.intino.ness.box.slack.Helper.findTank;

public class ReflowSession implements Consumer {
	private static final Logger logger = LoggerFactory.getLogger(ReflowSession.class);
	private final NessBox box;
	private ReflowProcess handler;
	private int blockSize;
	private Session session;
	private List<Tank> pausedTanks;

	public ReflowSession(NessBox box) {
		this.box = box;
	}

	@Override
	public void consume(Message message) {
		String text = textFrom(message);
		if (text.contains("quickReflow")) quickReflow(message);
		if (text.contains("startQuickReflow")) startQuickReflow(message);
		if (text.contains("blockSize")) defineReflow(text);
		else if (text.contains("finish")) finish();
		else if (text.contains("ready") && this.handler != null) ready(message);
		else if (!text.contains("ready") && handler != null) next();
	}

	private void startQuickReflow(Message message) {
		pauseTanks();
		replyTo(message, "ack");
	}

	private void quickReflow(Message message) {
		try {
			replyTo(message, "file://" + new File(box.workspace()).getCanonicalPath());
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void defineReflow(String text) {
		if (this.handler != null) return;
		Reflow reflow = new Gson().fromJson(text, Reflow.class);
		this.blockSize = reflow.blockSize();
		createSession(reflow);
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
				logger.info("Ready to reflow");
			} catch (JMSException e) {
				logger.error(e.getMessage(), e);
			}
		}).start();
	}

	private void createSession(Reflow reflow) {
		logger.info("Shutting down actual session");
		box.restartBus(false);
		pauseTanks();
		final List<Tank> tanks = collectTanks(reflow.tanks());
		this.session = box.busManager().transactedSession();
		this.handler = new ReflowProcess(session, tanks, reflow.from(), reflow.blockSize());
		for (Tank tank : tanks) new SortTankAction(box, tank, Instant.now()).execute();
		logger.info("Reflow session created");
	}

	private void next() {
		logger.info("sending next block of " + blockSize + " messages");
		handler.next();
	}

	private void finish() {
		logger.info("Reflow session finished");
		this.handler = null;
		pausedTanks.forEach(t -> new ResumeTankAction(box, t.qualifiedName()).execute(t));
//		if(box.busService().isPersistent()) box.restartBus(true); TODO falla al arrancar con persistencia
	}

	private void pauseTanks() {
		pausedTanks = box.datalake().tankList().stream().filter(AbstractTank::active).collect(Collectors.toList());
		pausedTanks.forEach(t -> new PauseTankAction(box, t.qualifiedName()).execute(t));
	}

	private void replyTo(Message request, String reply) {
		try {
			box.busManager().getQueueProducer(((ActiveMQDestination) request.getJMSReplyTo()).getPhysicalName()).produce(createMessageFor(reply));
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private List<Tank> collectTanks(List<String> tanks) {
		List<Tank> realTanks = new ArrayList<>();
		if (tanks.get(0).equalsIgnoreCase("all")) return box.datalake().tankList();
		for (String tank : tanks) {
			Tank realTank = findTank(box.datalake(), tank);
			if (realTank != null) realTanks.add(realTank);
		}
		return realTanks;
	}
}
