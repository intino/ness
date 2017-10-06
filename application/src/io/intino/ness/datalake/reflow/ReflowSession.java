package io.intino.ness.datalake.reflow;

import com.google.gson.Gson;
import io.intino.konos.jms.Consumer;
import io.intino.ness.box.NessBox;
import io.intino.ness.box.schemas.Reflow;

import javax.jms.Message;

public class ReflowSession implements Consumer {

	private final NessBox box;
	private ReflowProcessHandler manager;
	private int blockSize;

	public ReflowSession(NessBox box) {
		this.box = box;
	}

	@Override
	public void consume(Message message) {
		String json = Consumer.textFrom(message);
		if (json.contains("blockSize")) {
			if (this.manager != null) return;
			createSession(new Gson().fromJson(json, Reflow.class));
//			next();
		} else next();
	}

	private void createSession(Reflow reflow) {
		box.busManager().stopPersistence();
		this.manager = new ReflowProcessHandler(box, reflow.tanks(), reflow.blockSize());
		this.blockSize = reflow.blockSize();
	}

	private void next() {
		manager.next();
		if (manager.finished()) {
			box.busManager().startPersistence();
			this.manager = null;
		}
	}
}
