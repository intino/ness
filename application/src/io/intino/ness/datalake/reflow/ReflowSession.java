package io.intino.ness.datalake.reflow;

import com.google.gson.Gson;
import io.intino.konos.jms.Consumer;
import io.intino.konos.jms.QueueProducer;
import io.intino.ness.box.NessBox;
import io.intino.ness.box.actions.PauseTankAction;
import io.intino.ness.box.actions.ResumeTankAction;
import io.intino.ness.box.schemas.Reflow;
import io.intino.ness.graph.Tank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import static io.intino.konos.jms.Consumer.textFrom;
import static io.intino.konos.jms.MessageFactory.createMessageFor;

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
		if (text.contains("blockSize")) defineReflow(text);
		else if (text.contains("finish")) finish();
		else if (text.contains("ready") && this.handler != null) ready(message);
		else if (!text.contains("ready") && handler != null) next();
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
		for (Tank tank : handler.tanks()) {
			final ResumeTankAction action = new ResumeTankAction();
			action.box = box;
			action.tank = tank.qualifiedName();
			action.execute();
		}
		this.handler = null;
	}

	private void createSession(Reflow reflow) {
		logger.info("Shutting down actual session");
		for (String tank : reflow.tanks()) {
			final PauseTankAction action = new PauseTankAction();
			action.box = box;
			action.tank = tank;
			action.execute();
		}
		restartBusWithOutPersistence();
		this.handler = new ReflowProcessHandler(box, reflow.tanks(), reflow.blockSize());
		logger.info("Reflow session created");
	}

	private void next() {
		logger.info("sending next block of " + blockSize + " messages");
		handler.next();
	}

	private void restartBusWithOutPersistence() {
		box.restartBus(false);
	}

	private void restart() {
		box.restartBus(true);
	}
}
