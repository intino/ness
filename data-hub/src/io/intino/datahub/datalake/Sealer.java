package io.intino.datahub.datalake;

import io.intino.alexandria.Timetag;
import io.intino.alexandria.ingestion.EventSession;
import io.intino.alexandria.ingestion.SessionHandler;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.Message;
import io.intino.alexandria.message.MessageReader;
import io.intino.alexandria.sealing.SessionSealer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Objects;

public class Sealer {

	private final SessionSealer sealer;
	private final File brokerStageDirectory;

	public Sealer(SessionSealer sealer, File brokerStageDirectory) {
		this.sealer = sealer;
		this.brokerStageDirectory = brokerStageDirectory;
	}

	public void execute() {
		Logger.info("Starting seal of tanks");
		new Thread(() -> {
			pushTemporalSessions();
			sealer.seal();
			Logger.info("Sealing of tanks finished successfully");
		}).start();
	}

	private void pushTemporalSessions() {
		try {
			for (File file : Objects.requireNonNull(brokerStageDirectory.listFiles(f -> f.getName().endsWith(".inl")))) {
				String name = file.getName().replace(".inl", "");
				String[] split = name.split("#");
				SessionHandler handler = new SessionHandler();
				EventSession eventSession = handler.createEventSession();
				for (Message message : new MessageReader(new BufferedInputStream(new FileInputStream(file))))
					eventSession.put(split[0], new Timetag(split[1]), message);
				eventSession.close();
				handler.pushTo(brokerStageDirectory);
				file.delete();
			}
		} catch (FileNotFoundException e) {
			Logger.error(e);
		}
	}
}
