package io.intino.ness.datalake.reflow;

import com.google.gson.Gson;
import io.intino.konos.jms.Consumer;
import io.intino.konos.jms.QueueProducer;
import io.intino.ness.box.NessBox;
import io.intino.ness.box.schemas.Reflow;
import io.intino.ness.graph.Tank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import static io.intino.konos.jms.Consumer.textFrom;
import static io.intino.konos.jms.MessageFactory.createMessageFor;
import static io.intino.ness.box.slack.Helper.findTank;

public class ReflowSession implements Consumer {
	private static final Logger logger = LoggerFactory.getLogger(ReflowSession.class);
	private final NessBox box;
	private ReflowProcessHandler handler;
	private int blockSize;

	public ReflowSession(NessBox box) {
		this.box = box;
	}

	@Override
	public void consume(Message message) {
		String text = textFrom(message);
		if (text.contains("blockSize")) {
			if (this.handler != null) return;
			Reflow reflow = new Gson().fromJson(text, Reflow.class);
			this.blockSize = reflow.blockSize();
			createSession(reflow);
		} else if (text.contains("finish")) finish();
		else if (text.contains("ready") && this.handler != null) {
			ready(message);
		} else if (!text.contains("ready") && handler != null) next();
	}

	private void createSession(Reflow reflow) {
		logger.info("Shutting down actual session");
		for (String tank : reflow.tanks()) box.datalakeManager().stopTank(findTank(box, tank));
		restartBusWithOutPersistence();
		this.handler = new ReflowProcessHandler(box, reflow.tanks(), reflow.blockSize());
		logger.info("Reflow session created");
	}

	private void ready(final Message message) {
		new Thread(() -> {
			try {
				Session session = this.handler.session();
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

	private void finish() {
		logger.info("Reflow session finished");
		restart();
		for (Tank tank : handler.tanks()) box.datalakeManager().startTank(tank);
		this.handler = null;
	}

	private void next() {
		logger.info("sending next block of " + blockSize + " messages");
		handler.next();
	}

	private void restartBusWithOutPersistence() {
		box.restartBusWithoutPersistence();
	}

	private void restart() {
		box.restartBus();
	}
}
