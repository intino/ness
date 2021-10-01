package io.intino.datahub.box.actions;

import io.intino.alexandria.logger.Logger;
import io.intino.datahub.box.DataHubBox;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class SealAction {
	public static final String MESSAGE_PREFIX = "Sealed until ";
	private static final Object monitor = new Object();
	public DataHubBox box;

	public SealAction() {
	}

	public SealAction(DataHubBox box) {
		this.box = box;
	}

	public String execute() {
		synchronized (monitor) {
			try {
				box.brokerSessions().push();
				box.lastSeal(Instant.now());
				cleanStage();
				box.sessionSealer().seal();
				if (box.graph().datalake().backup() == null) cleanTreated();
				Logger.info("Finished sealing!");
				return MESSAGE_PREFIX + box.lastSeal().toString();
			} catch (Throwable e) {
				Logger.error(e);
				return "Error sealing: " + e.getMessage();
			}
		}
	}

	public String execute(String stage) {
		synchronized (monitor) {
			File subStage = new File(box.stageDirectory(), stage);
			final Instant now = Instant.now();
			if (subStage.exists()) {
				box.lastSeal(Instant.now());
				box.sessionSealer(subStage).seal();
			}
			Logger.info("Finished sealing of stage " + stage);
			return MESSAGE_PREFIX + now.toString();
		}
	}


	private void cleanStage() {
		for (File file : Objects.requireNonNull(box.stageDirectory().listFiles()))
			if (file.isDirectory() && Objects.requireNonNull(file.listFiles()).length == 0) try {
				FileUtils.deleteDirectory(file);
			} catch (IOException e) {
				Logger.error(e);
			}
	}

	private void cleanTreated() {
		Instant lastWeek = Instant.now().minus(7, ChronoUnit.DAYS);
		FileUtils.listFiles(box.stageDirectory(), new String[]{"treated"}, true).stream()
				.filter(f -> Instant.ofEpochMilli(f.lastModified()).isBefore(lastWeek))
				.forEach(File::delete);
	}
}