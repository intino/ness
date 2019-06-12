package io.intino.ness.sealing;

import io.intino.ness.datalake.file.FileDatalake;

import java.io.File;

public class FileSessionManager implements SessionManager {
	private final File stageFolder;
	private final FileStage stage;
	private final FileDatalake datalake;

	public FileSessionManager(FileDatalake datalake, File stageFolder) {
		this.datalake = datalake;
		this.stageFolder = stageFolder;
		this.stage = new FileStage(stageFolder);
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
		SetSessionManager.seal(stageFolder, datalake.setStoreFolder(), tempFolder());
	}

	private void sealEvents() {
		EventSessionManager.seal(stageFolder, datalake.eventStoreFolder(), tempFolder());
	}

	private File tempFolder() {
		File temp = new File(this.stageFolder, "temp");
		temp.mkdir();
		return temp;
	}
}