package io.intino.ness.datalakeinspector.box.regenerator;

import io.intino.alexandria.datalake.file.FileDatalake;
import io.intino.alexandria.logger.Logger;

import java.io.File;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public class Regenerator {
	private final FileDatalake datalake;
	private final File sessionsBackupDirectory;
	private final File reviewsDirectory;

	public Regenerator(FileDatalake datalake, File sessionsBackupDirectory, File reviewsDirectory) {
		this.datalake = datalake;
		this.sessionsBackupDirectory = sessionsBackupDirectory;
		this.reviewsDirectory = reviewsDirectory;
	}

	public List<File> review(Mapper mapper) {
		String ts = ts();
		Logger.info("Executing Regeneration review with mapper " + mapper.getClass().getSimpleName());
		File datalakeReport = new DatalakeRegenerator(datalake, reviewsDirectory, ts).review(mapper);
		Logger.info("Finished review on datalake");
		if (sessionsBackupDirectory != null) {
			File sessionsReport = new SessionRegenerator(datalake, sessionsBackupDirectory, reviewsDirectory).review(mapper);
			Logger.info("Finished Regeneration review");
			return Arrays.asList(datalakeReport, sessionsReport);
		}
		Logger.info("Finished Regeneration revise");
		return List.of(datalakeReport);
	}

	public List<File> revise(Mapper mapper) {
		String ts = ts();
		Logger.info("Executing Regeneration revise with mapper " + mapper.getClass().getSimpleName());
		File datalakeReport = new DatalakeRegenerator(datalake, reviewsDirectory, ts).revise(mapper);
		Logger.info("Finished revise on datalake");
		if (sessionsBackupDirectory != null) {
			File sessionsReport = new SessionRegenerator(datalake, sessionsBackupDirectory, reviewsDirectory).revise(mapper);
			Logger.info("Finished Regeneration revise");
			return Arrays.asList(datalakeReport, sessionsReport);
		}
		Logger.info("Finished Regeneration revise");
		return List.of(datalakeReport);
	}

	private String ts() {
		return Instant.now().toString().replaceAll("-|:", "").replace("T", "").substring(0, 14);
	}
}
