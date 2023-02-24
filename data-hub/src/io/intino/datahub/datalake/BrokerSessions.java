package io.intino.datahub.datalake;

import io.intino.alexandria.Fingerprint;
import io.intino.alexandria.Session;
import io.intino.alexandria.event.Event;
import io.intino.alexandria.event.message.MessageEvent;
import io.intino.alexandria.ingestion.EventSession;
import io.intino.alexandria.ingestion.SessionHandler;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.Message;
import io.intino.alexandria.message.MessageReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;

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
			SessionHandler handler = new SessionHandler(stageDirectory);
			File tmp = new File(stageDirectory, "tmp");
			tmp.mkdirs();
			moveToTmp(tmp);
			for (File file : requireNonNull(tmp.listFiles(f -> f.getName().endsWith(Session.SessionExtension)))) {
				MessageReader messages = new MessageReader(new FileInputStream(file));
				EventSession eventSession = handler.createEventSession();
				Fingerprint fingerprint = Fingerprint.of(file);
				for (Message message : messages)
					eventSession.put(fingerprint.tank(), fingerprint.source(), fingerprint.timetag(), Event.Format.Message, new MessageEvent(message));
				messages.close();
				eventSession.close();
				file.delete();
			}
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private void moveToTmp(File tmp) {
		for (File file : requireNonNull(brokerStageDirectory.listFiles(f -> f.getName().endsWith(Session.SessionExtension))))
			moveToTmp(file, tmp);
	}

	private void moveToTmp(File file, File tmp) {
		try {
			Files.move(file.toPath(), new File(tmp, file.getName()).toPath());
		} catch (IOException e) {
			Logger.error(e);
		}
	}
}
