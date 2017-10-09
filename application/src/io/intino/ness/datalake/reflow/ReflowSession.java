package io.intino.ness.datalake.reflow;

import com.google.gson.Gson;
import io.intino.konos.jms.Consumer;
import io.intino.ness.box.NessBox;
import io.intino.ness.box.schemas.Reflow;
import io.intino.ness.graph.Tank;

import javax.jms.Message;

import static io.intino.ness.box.slack.Helper.findTank;

public class ReflowSession implements Consumer {

	private final NessBox box;
	private ReflowProcessHandler handler;

	public ReflowSession(NessBox box) {
		this.box = box;
	}

	@Override
	public void consume(Message message) {
		String json = Consumer.textFrom(message);
		if (json.contains("blockSize")) {
			if (this.handler != null) return;
			createSession(new Gson().fromJson(json, Reflow.class));
		} else next();
	}

	private void createSession(Reflow reflow) {
		for (String tank : reflow.tanks()) box.datalakeManager().stopFeed(findTank(box, tank));
		restartBusWithOutPersistence();
		this.handler = new ReflowProcessHandler(box, reflow.tanks(), reflow.blockSize());
	}

	private void next() {
		handler.next();
		if (handler.finished()) {
			restart();
			for (Tank tank : handler.tanks()) box.datalakeManager().feed(tank);
			this.handler = null;
		}
	}

	private void restartBusWithOutPersistence() {
		box.restartBusWithoutPersistence();
	}

	private void restart() {
		box.restartBus();
	}
}
