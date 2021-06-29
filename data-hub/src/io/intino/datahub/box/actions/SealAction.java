package io.intino.datahub.box.actions;

import io.intino.alexandria.logger.Logger;
import io.intino.datahub.box.DataHubBox;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class SealAction {
	private static final AtomicBoolean started = new AtomicBoolean(false);
	private static final Object monitor = new Object();
	public DataHubBox box;

	public SealAction() {
	}

	public SealAction(DataHubBox box) {
		this.box = box;
	}

	public String execute() {
		synchronized (monitor) {
			if (started.get()) return "Sealing already started";
			try {
				started.set(true);
				box.brokerSessions().push();
				cleanStage();
				box.sessionSealer().seal();
				Logger.info("Finished sealing!");
				started.set(false);
			} catch (Throwable e) {
				Logger.error(e);
			}
			return "Finished sealing!";
		}
	}

	public synchronized void execute(String stage) {
		if (started.get()) return;
		started.set(true);
		File subStage = new File(box.stageDirectory(), stage);
		if (subStage.exists()) box.sessionSealer(subStage).seal();
		Logger.info("Finished sealing of stage " + stage);
		started.set(false);
	}


	private void cleanStage() {
		try {
			FileUtils.deleteDirectory(box.stageDirectory());
			box.stageDirectory().mkdirs();
		} catch (IOException e) {
			Logger.error(e);
		}
	}
}