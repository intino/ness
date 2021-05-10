package io.intino.datahub.datalake;

import io.intino.alexandria.Session;
import io.intino.alexandria.Timetag;
import io.intino.alexandria.event.Event;
import io.intino.alexandria.ingestion.EventSession;
import io.intino.alexandria.ingestion.SessionHandler;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.Message;
import io.intino.alexandria.message.MessageReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;

import static java.util.Objects.requireNonNull;

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
			File[] files = requireNonNull(brokerStageDirectory.listFiles(f -> f.getName().endsWith(".inl")));
			for (File file : files) {
				String name = file.getName().replace(".inl", "");
				String[] split = name.split("#");
				EventSession eventSession = handler.createEventSession();
				for (Message message : new MessageReader(new FileInputStream(file)))
					eventSession.put(split[0], new Timetag(split[1]), new Event(message));
				eventSession.close();
			}
			new SessionHandler(brokerStageDirectory).pushTo(stageDirectory);
			Arrays.stream(requireNonNull(brokerStageDirectory.listFiles(f -> f.getName().endsWith(Session.EventSessionExtension)))).forEach(File::delete);
			for (File file : files) file.delete();
		} catch (FileNotFoundException e) {
			Logger.error(e);
		}
	}
}
