package io.intino.ness.datalake.reflow;

import com.google.gson.Gson;
import io.intino.konos.jms.Consumer;
import io.intino.konos.jms.QueueProducer;
import io.intino.ness.box.NessBox;
import io.intino.ness.box.actions.PauseTankAction;
import io.intino.ness.box.actions.ResumeTankAction;
import io.intino.ness.box.actions.SortTankAction;
import io.intino.ness.box.schemas.ReflowConfiguration;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.intino.konos.jms.Consumer.textFrom;
import static io.intino.konos.jms.MessageFactory.createMessageFor;
import static io.intino.ness.box.slack.Helper.findTank;
import static java.util.stream.Collectors.toMap;

public class ReflowService implements Consumer {
	private static final Logger logger = LoggerFactory.getLogger(ReflowService.class);
	private final NessBox box;
	private ReflowProcess handler;
	private int blockSize;
	private Session session;
	private List<Tank> pausedTanks;

	public ReflowService(NessBox box) {
		this.box = box;
	}

	public void consume(Message message) {
		String text = textFrom(message);
		if (text.contains("blockSize")) defineReflow(text);
		else if (text.contains("quickReflow")) quickReflow(message);
		else if (text.contains("startQuickReflow")) startQuickReflow(message);
		else if (text.contains("ready") && this.handler != null) ready(message);
		else if (!text.contains("ready") && handler != null) next();
		else if (text.contains("finish")) finish();
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
		ReflowConfiguration reflow = new Gson().fromJson(text, ReflowConfiguration.class);
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

	private void createSession(ReflowConfiguration reflow) {
		logger.info("Shutting down actual session");
		box.restartBus(false);
		pauseTanks();
		final Map<Tank, Instant> tanks = collectTanks(reflow.tankList());
		this.session = box.busManager().transactedSession();
		this.handler = new ReflowProcess(session, tanks, reflow.blockSize());
		for (Tank tank : tanks.keySet()) new SortTankAction(box, tank, Instant.now()).execute();
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

	private Map<Tank, Instant> collectTanks(List<ReflowConfiguration.Tank> tanks) {
		return tanks.stream().collect(toMap(t -> findTank(box.datalake(), t.name()), ReflowConfiguration.Tank::from));
	}
}
