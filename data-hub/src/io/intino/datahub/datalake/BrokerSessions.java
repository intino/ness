package io.intino.datahub.datalake;

import io.intino.alexandria.Fingerprint;
import io.intino.alexandria.event.Event.Format;
import io.intino.alexandria.event.message.MessageEvent;
import io.intino.alexandria.event.resource.ResourceEventReader;
import io.intino.alexandria.ingestion.EventSession;
import io.intino.alexandria.ingestion.SessionHandler;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.Message;
import io.intino.alexandria.message.MessageReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;

import static io.intino.alexandria.Session.SessionExtension;
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
			moveTo(tmp);
			for (File file : requireNonNull(tmp.listFiles(f -> f.getName().endsWith(Format.Message + SessionExtension) || f.getName().endsWith(Format.Measurement + SessionExtension))))
				processMessageAndMeasurements(handler, file);
			for (File file : requireNonNull(tmp.listFiles(f -> f.getName().endsWith(Format.Resource + SessionExtension))))
				processResources(handler, file);
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	private void processResources(SessionHandler handler, File file) {
		EventSession eventSession = handler.createEventSession();
		try (ResourceEventReader resources = new ResourceEventReader(file)) {
			Fingerprint fingerprint = Fingerprint.of(file);
			resources.forEachRemaining(e -> {
				try {
					eventSession.put(fingerprint.tank(), fingerprint.source(), fingerprint.timetag(), Format.Resource, e);
				} catch (IOException ex) {
					Logger.error(ex);
				}
			});
		} catch (Exception e) {
			Logger.error(e);
		} finally {
			eventSession.close();
		}
		file.delete();
	}

	private static void processMessageAndMeasurements(SessionHandler handler, File file) throws Exception {
		EventSession eventSession = handler.createEventSession();
		try (MessageReader messages = new MessageReader(new FileInputStream(file))) {
			Fingerprint fingerprint = Fingerprint.of(file);
			for (Message message : messages)
				eventSession.put(fingerprint.tank(), fingerprint.source(), fingerprint.timetag(), Format.Message, new MessageEvent(message));
		} finally {
			eventSession.close();
		}
		file.delete();
	}

	private void moveTo(File tmp) {
		if (!brokerStageDirectory.exists()) return;
		for (File file : requireNonNull(brokerStageDirectory.listFiles(f -> f.getName().endsWith(SessionExtension))))
			moveTo(file, tmp);
	}

	private void moveTo(File file, File tmp) {
		try {
			Files.move(file.toPath(), new File(tmp, file.getName()).toPath());
		} catch (IOException e) {
			Logger.error(e);
		}
	}
}
