package io.intino.ness.datalake;

import java.io.File;

public class FileDatalake implements Datalake {
	private final File root;

	public FileDatalake(File root) {
		this.root = root;
		checkStore();
	}

	private void checkStore() {
		//TODO
	}

	@Override
	public EventStore eventStore() {
		return new FileEventStore(eventStoreFolder());
	}

	@Override
	public SetStore setStore() {
		return new FileSetStore(setStoreFolder());
	}

	public File root() {
		return root;
	}

	public File eventStoreFolder() {
		return new File(root, EventStoreFolder);
	}

	public File setStoreFolder() {
		return new File(root, SetStoreFolder);
	}

	public File stageFolder() {
		return new File(root, StageFolder);
	}

}