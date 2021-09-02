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
import java.io.IOException;
import java.nio.file.Files;
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
			File tmp = new File(stageDirectory, "tmp");
			tmp.mkdirs();
			for (File file : requireNonNull(brokerStageDirectory.listFiles(f -> f.getName().endsWith(".inl"))))
				moveToTmp(file, tmp);
			File[] tmpFiles = requireNonNull(tmp.listFiles(f -> f.getName().endsWith(".inl")));
			for (File file : tmpFiles) {
				String name = file.getName().replace(".inl", "");
				String[] split = name.split("#");
				EventSession eventSession = handler.createEventSession();
				MessageReader messages = new MessageReader(new FileInputStream(file));
				for (Message message : messages)
					eventSession.put(split[0], new Timetag(split[1]), new Event(message));
				messages.close();
				eventSession.close();
				file.delete();
			}
			new SessionHandler(tmp).pushTo(stageDirectory);
		} catch (FileNotFoundException e) {
			Logger.error(e);
		}
	}

	private void moveToTmp(File file, File tmp) {
		try {
			Files.move(file.toPath(), new File(tmp, file.getName()).toPath());
		} catch (IOException e) {
			Logger.error(e);
		}
	}
}
