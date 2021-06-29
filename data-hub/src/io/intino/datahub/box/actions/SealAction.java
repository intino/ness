package io.intino.datahub.box.actions;

import io.intino.alexandria.logger.Logger;
import io.intino.datahub.box.DataHubBox;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
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
				if (box.graph().datalake().backup() == null) cleanTreated();
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

	private void cleanTreated() {
		Instant lastWeek = Instant.now().minus(7, ChronoUnit.DAYS);
		FileUtils.listFiles(box.stageDirectory(), new String[]{"treated"}, true).forEach(f -> {
			if (Instant.ofEpochMilli(f.lastModified()).isBefore(lastWeek)) f.delete();
		});

	}
}