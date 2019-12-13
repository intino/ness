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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Objects;

public class BrokerSessionSealer implements SessionSealer {
	private final SessionSealer sealer;
	private final File brokerStageDirectory;

	public BrokerSessionSealer(FileDatalake datalake, File brokerStageDirectory) {
		this.sealer = new FileSessionSealer(datalake, brokerStageDirectory);
		this.brokerStageDirectory = brokerStageDirectory;
	}

	@Override
	public void seal(List<Datalake.EventStore.Tank> avoidSorting) {
		Logger.info("Starting seal broker events");
		new Thread(() -> {
			pushTemporalSessions();
			sealer.seal();
			Logger.info("Sealing of tanks finished successfully");
		}).start();
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
}
