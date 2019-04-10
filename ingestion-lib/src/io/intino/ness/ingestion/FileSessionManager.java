package io.intino.ness.ingestion;

import io.intino.ness.datalake.file.FileDatalake;

import java.io.File;
import java.util.stream.Stream;

public class FileSessionManager implements SessionManager {
	private final File sessionsFolder;
	private final FileStage stage;
	private final FileDatalake datalake;

	public FileSessionManager(FileDatalake datalake, File sessionsFolder) {
		this.datalake = datalake;
		this.sessionsFolder = sessionsFolder;
		this.stage = new FileStage(datalake.stageFolder(), sessionsFolder);
	}

	public void push(Stream<Session> sessions) {
		stage.push(sessions);
	}

	public void seal() {
		sealEvents();
		sealSets();
		makeSetIndexes();
		stage.clear();
	}

	private void makeSetIndexes() {
		new SetIndexer(datalake.setStoreFolder()).make();
	}

	private void sealSets() {
		SetSessionManager.seal(datalake.stageFolder(), datalake.setStoreFolder(), tempFolder());
	}

	private void sealEvents() {
		EventSessionManager.seal(datalake.stageFolder(), datalake.eventStoreFolder(), tempFolder());
	}

	private File tempFolder() {
		File temp = new File(this.sessionsFolder, "temp");
		temp.mkdir();
		return temp;
	}
}