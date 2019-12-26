package io.intino.datahub.datalake;

import io.intino.alexandria.Timetag;
import io.intino.alexandria.datalake.Datalake;
import io.intino.alexandria.datalake.file.FileDatalake;
import io.intino.alexandria.event.Event;
import io.intino.alexandria.ingestion.EventSession;
import io.intino.alexandria.ingestion.SessionHandler;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.Message;
import io.intino.alexandria.message.MessageReader;
import io.intino.alexandria.sealing.FileSessionSealer;
import io.intino.alexandria.sealing.SessionSealer;

import java.io.*;
import java.nio.file.Files;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class BrokerSessionSealer implements SessionSealer {
	private final SessionSealer sealer;
	private final File brokerStageDirectory;
	private final File stageDirectory;

	public BrokerSessionSealer(FileDatalake datalake, File brokerStageDirectory, File stageDirectory) {
		this.stageDirectory = stageDirectory;
		this.sealer = new FileSessionSealer(datalake, brokerStageDirectory);
		this.brokerStageDirectory = brokerStageDirectory;
	}

	@Override
	public void seal(List<Datalake.EventStore.Tank> avoidSorting) {
		Logger.info("Starting seal broker events");
		new Thread(() -> {
			pushTemporalSessions();
			sealer.seal();
			File[] files = treatedSessions();
			if (files.length > 0) {
				File treatedDirectory = new File(stageDirectory, "treated.broker_" + instant());
				treatedDirectory.mkdir();
				moveTreatedSessions(treatedSessions(), treatedDirectory);
			}
			Logger.info("Sealing of tanks finished successfully");
		}).start();
	}

	private void moveTreatedSessions(File[] treatedSessions, File treatedDirectory) {
		try {
			for (File treatedSession : treatedSessions)
				Files.move(treatedSession.toPath(), new File(treatedDirectory, treatedSession.getName()).toPath());
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private File[] treatedSessions() {
		return brokerStageDirectory.listFiles(f -> f.isFile() && f.getName().endsWith(".treated"));
	}

	private void pushTemporalSessions() {
		try {
			SessionHandler handler = new SessionHandler(brokerStageDirectory);
			EventSession eventSession = handler.createEventSession();
			for (File file : Objects.requireNonNull(brokerStageDirectory.listFiles(f -> f.getName().endsWith(".inl")))) {
				String name = file.getName().replace(".inl", "");
				String[] split = name.split("#");
				for (Message message : new MessageReader(new BufferedInputStream(new FileInputStream(file))))
					eventSession.put(split[0], new Timetag(split[1]), new Event(message));
				eventSession.close();
				file.delete();
			}
		} catch (FileNotFoundException e) {
			Logger.error(e);
		}
	}

	private String instant() {
		String instant = Instant.now().toString().replace(":", "-");
		return instant.substring(0, instant.indexOf("."));
	}
}
