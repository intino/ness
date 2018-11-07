package io.intino.ness.core.fs;

import io.intino.alexandria.logger.Logger;
import io.intino.ness.core.Blob;
import io.intino.ness.core.Datalake;
import io.intino.ness.core.sessions.EventSessionManager;
import io.intino.ness.core.sessions.SetSessionManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Stream;

import static java.time.Instant.now;

public class FSDatalake implements Datalake {
	private static final String EventStoreFolder = "events";
	private static final String SetStoreFolder = "sets";
	private static final String StageFolder = "stage";
	private static final String TreatedFolder = "treated";

	private File root;

	public FSDatalake(File root) {
		this.root = root;
		this.mkdirs();
	}

	@Override
	public Connection connection() {
		return new Connection() {
			@Override
			public void connect(String... args) {
			}

			@Override
			public void disconnect() {
			}
		};
	}

	@Override
	public EventStore eventStore() {
		return new FSEventStore(eventStoreFolder());
	}

	@Override
	public SetStore setStore() {
		return new FSSetStore(setStoreFolder());
	}

	@Override
	public void push(Stream<Blob> blobs) {
		blobs.forEach(this::process);
	}

	private void process(Blob blob) {
		if (blob.type() == Blob.Type.event) EventSessionManager.push(stageFolder(), blob);
		else SetSessionManager.push(blob, stageFolder());
	}

	@Override
	public void seal() {
		EventSessionManager.seal(stageFolder(), eventStoreFolder());
		SetSessionManager.seal(stageFolder(), setStoreFolder());
		moveToTreated();
	}

	private void moveToTreated() {
		File treatedFolder = new File(treatedFolder(), sealDateFolderName());
		treatedFolder.mkdirs();
		FS.filesIn(treatedFolder(), File::isFile).forEach(f -> move(f, treatedFolder));
	}

	private void move(File stageFile, File treatedFolder) {
		try {
			Files.move(stageFile.toPath(), new File(treatedFolder, stageFile.getName()).toPath());
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private String sealDateFolderName() {
		return now().toString().substring(0, 19).replaceAll("[:T\\-]", "");
	}

	private void mkdirs() {
		eventStoreFolder().mkdirs();
		setStoreFolder().mkdirs();
		stageFolder().mkdirs();
		treatedFolder().mkdirs();
	}

	private File eventStoreFolder() {
		return new File(root, EventStoreFolder);
	}

	private File setStoreFolder() {
		return new File(root, SetStoreFolder);
	}

	private File stageFolder() {
		return new File(root, StageFolder);
	}

	private File treatedFolder() {
		return new File(root, TreatedFolder);
	}
}
