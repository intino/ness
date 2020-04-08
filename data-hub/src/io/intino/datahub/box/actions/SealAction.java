package io.intino.datahub.box.actions;

import io.intino.alexandria.logger.Logger;
import io.intino.datahub.box.DataHubBox;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class SealAction {
	private static AtomicBoolean started = new AtomicBoolean(false);
	public DataHubBox box;

	public SealAction() {
	}

	public SealAction(DataHubBox box) {
		this.box = box;
	}

	public synchronized String execute() {
		if (started.get()) return "Sealing already started";
		started.set(true);
		box.brokerSessions().push();
		cleanStage();
		box.sessionSealer().seal();
		Logger.info("Finished sealing!");
		started.set(false);
		return "Finished sealing!";
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
		for (File file : Objects.requireNonNull(box.stageDirectory().listFiles())) {
			if (file.isDirectory() && Objects.requireNonNull(file.listFiles()).length == 0) {
				try {
					FileUtils.deleteDirectory(file);
				} catch (IOException e) {
					Logger.error(e);
				}
			}

		}
	}
}