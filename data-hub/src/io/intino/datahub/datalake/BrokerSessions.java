package io.intino.datahub.datalake;

import io.intino.alexandria.Timetag;
import io.intino.alexandria.event.Event;
import io.intino.alexandria.ingestion.EventSession;
import io.intino.alexandria.ingestion.SessionHandler;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.Message;
import io.intino.alexandria.message.MessageReader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Objects;

public class BrokerSessions {
	private final File brokerStageDirectory;
	private final File stageDirectory;

	public BrokerSessions(File brokerStageDirectory, File stageDirectory) {
		this.brokerStageDirectory = brokerStageDirectory;
		this.stageDirectory = stageDirectory;
	}

	public void push() {
		Logger.info("Pushing broker events");
		pushTemporalSessions();
		Logger.info("Pushed broker events");

	}

	private void pushTemporalSessions() {
		try {
			SessionHandler handler = new SessionHandler(brokerStageDirectory);
			for (File file : Objects.requireNonNull(brokerStageDirectory.listFiles(f -> f.getName().endsWith(".inl")))) {
				String name = file.getName().replace(".inl", "");
				String[] split = name.split("#");
				EventSession eventSession = handler.createEventSession();
				for (Message message : new MessageReader(new BufferedInputStream(new FileInputStream(file))))
					eventSession.put(split[0], new Timetag(split[1]), new Event(message));
				eventSession.close();
				file.delete();
			}
			new SessionHandler(brokerStageDirectory).pushTo(stageDirectory);
		} catch (FileNotFoundException e) {
			Logger.error(e);
		}
	}
}
