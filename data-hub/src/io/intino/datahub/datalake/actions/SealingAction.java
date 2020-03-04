package io.intino.datahub.datalake.actions;

import io.intino.alexandria.logger.Logger;
import io.intino.datahub.DataHub;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class SealingAction {
	private static AtomicBoolean started = new AtomicBoolean(false);
	public DataHub dataHub;

	public SealingAction(DataHub dataHub) {
		this.dataHub = dataHub;
	}

	public synchronized void execute() {
		if (started.get()) return;
		started.set(true);
		dataHub.brokerSessions().push();
		cleanStage();
		dataHub.sessionSealer().seal();
		Logger.info("Finished sealing!");
		started.set(false);
	}

	public synchronized void execute(String stage) {
		if (started.get()) return;
		started.set(true);
		File subStage = new File(dataHub.stage(), stage);
		if (subStage.exists()) dataHub.sessionSealer(subStage).seal();
		Logger.info("Finished sealing of stage " + stage);
		started.set(false);
	}


	private void cleanStage() {
		for (File file : Objects.requireNonNull(dataHub.stage().listFiles())) {
			if (file.isDirectory() && Objects.requireNonNull(file.listFiles()).length == 0) {
				try {
					FileUtils.deleteDirectory(file);
				} catch (IOException e) {
					Logger.error(e);
				}
			}

		}
	}

	public boolean isStarted() {
		return started.get();
	}
}