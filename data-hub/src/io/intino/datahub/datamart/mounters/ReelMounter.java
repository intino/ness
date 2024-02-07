package io.intino.datahub.datamart.mounters;

import io.intino.alexandria.event.Event;
import io.intino.alexandria.event.message.MessageEvent;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.Message;
import io.intino.datahub.datamart.MasterDatamart;
import io.intino.datahub.model.Attribute;
import io.intino.datahub.model.Datamart;
import io.intino.datahub.model.Reel;
import io.intino.sumus.chronos.ReelFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.intino.datahub.box.DataHubBox.REEL_EXTENSION;
import static io.intino.datahub.datamart.MasterDatamart.normalizePath;
import static io.intino.datahub.datamart.mounters.MounterUtils.copyOf;
import static io.intino.datahub.datamart.mounters.MounterUtils.tempDir;
import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class ReelMounter extends MasterDatamartMounter {

	private final File temp;

	public ReelMounter(MasterDatamart datamart) {
		super(datamart);
		temp = tempDir("reel_mounter");
	}

	@Override
	public void mount(Event event) {
		synchronized (datamart) {
			if (event instanceof MessageEvent e) mount(e.toMessage());
		}
	}

	@Override
	public void mount(Message message) {
		synchronized (datamart) {
			if (message == null) return;
			MessageEvent event = new MessageEvent(message);
			String subject = subject(event);
			try {
				update(reelFileWith(message.type(), subject), event);
			} catch (IOException e) {
				Logger.error(e);
			}
		}
	}

	@Override
	public List<String> destinationsOf(Message message) {
		return List.of(message.type() + "\0" + subject(new MessageEvent(message)));
	}

	String subject(MessageEvent event) {
		Datamart datamart = this.datamart.definition();
		Reel reel = datamart.reel(r -> r.tank().message().name$().equals(event.type()));
		return event.toMessage().get(reel.entitySource().name$()).asString();
	}

	protected void update(File reelFile, MessageEvent event) throws IOException {
		reelFile.getParentFile().mkdirs();
		File sessionFile = copyOf(temp, reelFile, ".session");
		try {
			Datamart datamart = this.datamart.definition();
			List<Reel> reels = datamart.reelList(r -> r.tank().message().name$().equals(event.type()));
			try (ReelFile.Session session = open(sessionFile).session()) {
				for (Reel reel : reels) {
					session.set(event.ts(), group(event, reel.groupSource()), mappingAttribute(event.toMessage(), reel));
				}
			}
			Files.move(sessionFile.toPath(), reelFile.toPath(), REPLACE_EXISTING, ATOMIC_MOVE);
		} catch (Exception e) {
			sessionFile.delete();
			throw e;
		}
	}

	String group(MessageEvent event, Attribute attribute) {
		Message.Value value = event.toMessage().get(attribute.name$());
		return !value.isNull() ? value.asString() : event.type().toLowerCase();
	}

	String[] mappingAttribute(Message message, Reel reel) {
		return values(message, reel.signals()).toArray(String[]::new);
	}

	private static Stream<String> values(Message message, Attribute from) {
		Message.Value value = message.get(from.name$());
		return !value.isNull() ? value.asList(String.class).stream() : Stream.empty();
	}

	private File reelFileWith(String type, String subject) {
		return new File(box().datamartReelsDirectory(datamart.name(), type), normalizePath(subject + REEL_EXTENSION));
	}

	private static ReelFile open(File file) throws IOException {
		try {
			if (file.exists()) return ReelFile.open(file);
			file.getParentFile().mkdirs();
			return ReelFile.create(file);
		} catch (Exception e) {
			throw new IOException("[" + Thread.currentThread().getName() + "]: Could not open reel file " + file.getAbsolutePath() + ": " + e.getMessage(), e);
		}
	}

	public static class Reflow extends ReelMounter implements AutoCloseable {

		private final Map<String, ReelFile.Session> sessions = new HashMap<>();

		public Reflow(MasterDatamart datamart) {
			super(datamart);
		}

		@Override
		public void mount(Message message) {
			if (message == null) return;
			try {
				MessageEvent event = new MessageEvent(message);
				String key = message.type() + subject(event);
				ReelFile.Session session = sessions.get(key);
				if (session == null) {
					session = createReelSession(message.type(), subject(event));
					sessions.put(key, session);
				}
				update(session, event);
			} catch (IOException e) {
				Logger.error(e);
			}
		}

		private ReelFile reelFile(String type, String subject) throws IOException {
			File sessionFile = new File(box().datamartReelsDirectory(datamart.name(), type), normalizePath(subject + REEL_EXTENSION + ".session"));
			if (sessionFile.exists()) sessionFile.delete();
			else sessionFile.getParentFile().mkdirs();
			return ReelFile.create(sessionFile);
		}

		private ReelFile.Session createReelSession(String type, String subject) throws IOException {
			return reelFile(type, subject).session();
		}

		private void update(ReelFile.Session session, MessageEvent event) throws IOException {
			Datamart datamart = this.datamart.definition();
			List<Reel> reels = datamart.reelList(r -> r.tank().message().name$().equals(event.type()));
			for (Reel reel : reels)
				session.set(event.ts(), group(event, reel.groupSource()), mappingAttribute(event.toMessage(), reel));
		}

		@Override
		public void close() {
			for (var session : sessions.values()) {
				try {
					session.close();
					File sessionFile = session.file();
					File reelFile = new File(sessionFile.getAbsolutePath().replace(".session", ""));
					Files.move(sessionFile.toPath(), reelFile.toPath(), REPLACE_EXISTING, ATOMIC_MOVE);
				} catch (Exception e) {
					Logger.error(e);
				}
			}
		}
	}
}