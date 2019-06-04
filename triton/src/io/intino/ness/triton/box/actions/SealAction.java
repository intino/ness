package io.intino.ness.triton.box.actions;

import io.intino.alexandria.Timetag;
import io.intino.alexandria.inl.Message;
import io.intino.alexandria.inl.MessageReader;
import io.intino.alexandria.logger.Logger;
import io.intino.ness.ingestion.EventSession;
import io.intino.ness.ingestion.SessionHandler;
import io.intino.ness.triton.box.TritonBox;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Objects;


public class SealAction {

	public TritonBox box;

	public SealAction(TritonBox box) {
		this.box = box;
	}

	public SealAction() {
	}

	public void execute() {
		Logger.info("Starting seal of tanks");
		new Thread(() -> {
			pauseTanks();
			pushTemporalSessions();
			restartTanks();
			box.sessionManager().seal();
			Logger.info("Sealing of tanks finished successfully");
		}).start();
	}

	private void pauseTanks() {
		box.datalake().eventStore().tanks().forEach(t -> new PauseTankAction(box, t.name()).execute());
	}

	private void restartTanks() {
		box.datalake().eventStore().tanks().forEach(t -> new ResumeTankAction(box, t.name()).execute());
	}

	private void pushTemporalSessions() {
		try {
			for (File file : Objects.requireNonNull(box.temporalSession().listFiles(f -> f.getName().endsWith(".inl")))) {
				String name = file.getName().replace(".inl", "");
				String[] split = name.split("#");
				SessionHandler sessionHandler = new SessionHandler();
				EventSession eventSession = sessionHandler.createEventSession();
				for (Message message : new MessageReader(new BufferedInputStream(new FileInputStream(file))))
					eventSession.put(split[0], new Timetag(split[1]), message);
				eventSession.close();
				box.sessionManager().push(sessionHandler.sessions());
				file.delete();
			}
		} catch (FileNotFoundException e) {
			Logger.error(e);
		}
	}
}