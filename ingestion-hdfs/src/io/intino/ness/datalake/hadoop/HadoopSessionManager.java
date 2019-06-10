package io.intino.ness.datalake.hadoop;

import io.intino.alexandria.logger.Logger;
import io.intino.ness.ingestion.Session;
import io.intino.ness.ingestion.SessionManager;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.util.stream.Stream;

public class HadoopSessionManager implements SessionManager {
	private final HadoopStage stage;
	private final FileSystem fs;
	private final Path sessionsPath;
	private final HadoopDatalake datalake;

	public HadoopSessionManager(HadoopDatalake datalake, FileSystem fs, Path treatedSessionsPath) {
		this.datalake = datalake;
		this.fs = fs;
		this.sessionsPath = treatedSessionsPath;
		mkdirs(treatedSessionsPath);
		this.stage = new HadoopStage(fs, datalake.stagePath(), treatedSessionsPath);
	}

	private void mkdirs(Path sessionsPath) {

		try {
			if (!fs.exists(sessionsPath))
				fs.mkdirs(sessionsPath);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public void push(Stream<Session> sessions) {
		stage.push(sessions);
	}

	public void seal() {
		new SessionSealer(fs, stage, datalake.eventStorePath(), datalake.setStorePath(), tempPath()).seal();
		stage.clear();
	}

	private Path tempPath() {
		return new Path(this.sessionsPath, "temp");
	}
}
