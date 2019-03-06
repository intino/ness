package io.intino.ness.core.local;

import io.intino.ness.core.Datalake;
import io.intino.ness.core.Session;
import io.intino.ness.core.local.sessions.EventSessionManager;
import io.intino.ness.core.local.sessions.SetSessionManager;

import java.io.File;
import java.util.stream.Stream;

public class LocalDatalake implements Datalake {
	private static String TempFolder = "temp";
	private File root;
	private final FileStage stage;

	public LocalDatalake(File root) {
		this.root = root;
		this.mkdirs();
		this.stage = new FileStage(stageFolder(), sessionsFolder());
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
		return new LocalEventStore(eventStoreFolder());
	}

	@Override
	public SetStore setStore() {
		return new LocalSetStore(setStoreFolder());
	}

	@Override
	public void push(Stream<Session> sessions) {
		stage.push(sessions);
	}

	@Override
	public void seal() {
		sealEvents();
		sealSets();
		stage.clear();
	}

	private void sealSets() {
		SetSessionManager.seal(stageFolder(), setStoreFolder(), tempFolder());
	}

	private void sealEvents() {
		EventSessionManager.seal(stageFolder(), eventStoreFolder(), tempFolder());
	}

	private void mkdirs() {
		eventStoreFolder().mkdirs();
		setStoreFolder().mkdirs();
		stageFolder().mkdirs();
		tempFolder().mkdirs();
		sessionsFolder().mkdirs();
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

	private File tempFolder() {
		return new File(root, TempFolder);
	}

	private File sessionsFolder() {
		return new File(root, SessionsFolder);
	}
}
