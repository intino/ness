package io.intino.datahub.datalake.regenerator;

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
		File sessionsReport = new SessionRegenerator(datalake, sessionsBackupDirectory, reviewsDirectory, ts).review(mapper);
		Logger.info("Finished Regeneration review");
		return Arrays.asList(datalakeReport, sessionsReport);
	}

	public List<File> revise(Mapper mapper) {
		String ts = ts();
		Logger.info("Executing Regeneration revise with mapper " + mapper.getClass().getSimpleName());
		File datalakeReport = new DatalakeRegenerator(datalake, reviewsDirectory, ts).revise(mapper);
		Logger.info("Finished revise on datalake");
		File sessionsReport = new SessionRegenerator(datalake, sessionsBackupDirectory, reviewsDirectory, ts).revise(mapper);
		Logger.info("Finished Regeneration");
		return Arrays.asList(datalakeReport, sessionsReport);
	}

	private String ts() {
		return Instant.now().toString().replaceAll("-|:", "").replace("T", "").substring(0, 14);
	}
}
