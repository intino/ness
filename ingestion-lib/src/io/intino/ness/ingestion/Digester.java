package io.intino.ness.ingestion;

import io.intino.ness.datalake.FileDatalake;

import java.io.File;
import java.util.stream.Stream;

public class Digester {

	private final File sessionsFolder;
	private final FileStage stage;
	private final FileDatalake datalake;

	public Digester(FileDatalake datalake, File sessionsFolder) {
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
		stage.clear();
	}

	private void sealSets() {
		SetSessionManager.seal(datalake.stageFolder(), datalake.setStoreFolder(), tempFolder());
	}

	private void sealEvents() {
		EventSessionManager.seal(datalake.stageFolder(), datalake.eventStoreFolder(), tempFolder());
	}

	private File tempFolder() {
		return new File(this.sessionsFolder, "temp");
	}
}
