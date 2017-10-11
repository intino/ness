package io.intino.ness.datalake.reflow;

import com.google.gson.Gson;
import io.intino.konos.jms.Consumer;
import io.intino.ness.box.NessBox;
import io.intino.ness.box.schemas.Reflow;
import io.intino.ness.graph.Tank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Message;

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
		String json = Consumer.textFrom(message);
		if (json.contains("blockSize")) {
			if (this.handler != null) return;
			Reflow reflow = new Gson().fromJson(json, Reflow.class);
			this.blockSize = reflow.blockSize();
			createSession(reflow);
		} else if (json.contains("finish")) finish();
		else next();
	}

	private void createSession(Reflow reflow) {
		logger.info("Reflow session created");
		for (String tank : reflow.tanks()) box.datalakeManager().stopFeed(findTank(box, tank));
		restartBusWithOutPersistence();
		this.handler = new ReflowProcessHandler(box, reflow.tanks(), reflow.blockSize());
	}

	private void finish() {
		logger.info("Reflow session finished");
		restart();
		for (Tank tank : handler.tanks()) box.datalakeManager().feed(tank);
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
