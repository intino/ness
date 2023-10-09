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
import smile.neighbor.lsh.Hash;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.intino.datahub.box.DataHubBox.REEL_EXTENSION;
import static io.intino.datahub.datamart.MasterDatamart.ChronosDirectory.normalizePath;

public class ReelMounter extends MasterDatamartMounter {

	public ReelMounter(MasterDatamart datamart) {
		super(datamart);
	}

	@Override
	public void mount(Event event) {
		synchronized (datamart) {
			if (event instanceof MessageEvent e) mount(e.toMessage());
		}
	}

	@Override
	public void mount(Message message) {
		if (message == null) return;
		MessageEvent event = new MessageEvent(message);
		String ss = withoutParameters(event.ss());
		ReelFile reelFile = datamart.reelStore().get(ss, ss);
		try {
			if (reelFile == null) reelFile = reelFile(message.type(), subject(event));
			update(reelFile, event);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	String subject(MessageEvent event) {
		Datamart datamart = this.datamart.definition();
		Reel reel = datamart.reel(r -> r.tank().message().name$().equals(event.type()));
		return event.toMessage().get(reel.entitySource().name$()).asString();
	}

	protected void update(ReelFile reelFile, MessageEvent event) throws IOException {
		Datamart datamart = this.datamart.definition();
		List<Reel> reels = datamart.reelList(r -> r.tank().message().name$().equals(event.type()));
		for (Reel reel : reels)
			reelFile.set(event.ts(), group(event, reel.groupSource()), mappingAttribute(event.toMessage(), reel));
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

	ReelFile reelFile(String type, String subject) throws IOException {
		File file = new File(box().datamartReelsDirectory(datamart.name(), type), normalizePath(subject + REEL_EXTENSION));
		file.getParentFile().mkdirs();
		return file.exists() ? ReelFile.open(file) : ReelFile.create(file);
	}

	String withoutParameters(String ss) {
		return ss.contains("?") ? ss.substring(0, ss.indexOf("?")) : ss;
	}

	public static class OfSingleReel extends ReelMounter implements AutoCloseable {

		private final String ss;
		private final ReelFile reelFile;
		private ReelFile.Session session;

		public OfSingleReel(MasterDatamart datamart, String tank, String ss) {
			super(datamart);
			this.ss = ss;
			try {
				this.reelFile = reelFile(tank, ss);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void mount(Message message) {
			MessageEvent event = new MessageEvent(message);
			if (!ss.equals(subject(event))) return;
			try {
				update(reelFile, event);
			} catch (IOException e) {
				Logger.error(e);
			}
		}

		@Override
		protected void update(ReelFile reelFile, MessageEvent event) throws IOException {
			Datamart datamart = this.datamart.definition();
			List<Reel> reels = datamart.reelList(r -> r.tank().message().name$().equals(event.type()));
			if(session == null) session = reelFile.session();
			for (Reel reel : reels) {
				session.set(event.ts(), group(event, reel.groupSource()), mappingAttribute(event.toMessage(), reel));
			}
		}

		@Override
		public void close() throws Exception {
			if(session != null) session.close();
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
				if(session == null) {
					session = createReelSession(message.type(), subject(event));
					sessions.put(key, session);
				}
				update(session, event);
			} catch (IOException e) {
				Logger.error(e);
			}
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
			for(var session : sessions.values()) {
				try {
					session.close();
				} catch (Exception e) {
					Logger.error(e);
				}
			}
		}
	}
}