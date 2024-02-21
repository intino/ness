package io.intino.ness.datalakeinspector.box.regenerator;

import io.intino.alexandria.Fingerprint;
import io.intino.alexandria.Session;
import io.intino.alexandria.Timetag;
import io.intino.alexandria.datalake.Datalake;
import io.intino.alexandria.datalake.Datalake.Store.Source;
import io.intino.alexandria.datalake.Datalake.Store.Tank;
import io.intino.alexandria.event.Event;
import io.intino.alexandria.event.message.MessageEvent;
import io.intino.alexandria.event.message.MessageEventReader;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.MessageWriter;
import io.intino.alexandria.zim.Zim;

import java.io.*;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Collection;

import static org.apache.commons.io.FileUtils.listFiles;

public class SessionRegenerator {

	private final Datalake datalake;
	private final File backupDirectory;
	private final File reviewDirectory;
	private final String ts;

	public SessionRegenerator(Datalake datalake, File backupDirectory, File reviewDirectory) {
		this.datalake = datalake;
		this.backupDirectory = backupDirectory;
		this.reviewDirectory = reviewDirectory;
		this.ts = ts();
	}

	public File review(Mapper mapper) {
		File reportFile = new File(reviewDirectory, mapperPrefixName(mapper) + ".html");
		RegeneratorReporter reporter = new RegeneratorReporter(reportFile);
		for (File session : sessions()) {
			if (notSuitable(session, mapper.filter())) continue;
			try (MessageEventReader reader = new MessageEventReader(session)) {
				reader.forEachRemaining(e -> {
					String before = e.toString();
					map(mapper, reporter, e, before);
				});
			} catch (Exception e) {
				Logger.error(e);
			}
		}
		reporter.commit();
		return reportFile;
	}

	public File revise(Mapper mapper) {
		File reportFile = new File(backupDirectory, mapperPrefixName(mapper) + ".html");
		RegeneratorReporter reporter = new RegeneratorReporter(reportFile);
		for (File session : sessions()) {
			if (notSuitable(session, mapper.filter())) continue;
			MessageWriter writer = new MessageWriter(zim(temp(session)));
			try (MessageEventReader reader = new MessageEventReader(session)) {
				reader.forEachRemaining(e -> {
					String before = e.toString();
					MessageEvent after = map(mapper, reporter, e, before);
					if (after != null) write(writer, after);
				});
			} catch (Exception e) {
				Logger.error(e);
			}
			close(writer);
			backupSourceSession(mapper, session);
			if (temp(session).length() > 20) move(temp(session), session);
			else temp(session).delete();
		}
		reporter.commit();
		return reportFile;
	}

	private boolean notSuitable(File session, Mapper.Filter filter) {
		Tank<MessageEvent> tank = tankOf(session);
		Source<MessageEvent> source = sourceOf(tank, session);
		return !filter.allow(tank) || !filter.allow(tank, source, timetagOf(session));
	}

	private MessageEvent map(Mapper mapper, RegeneratorReporter reporter, MessageEvent e, String before) {
		MessageEvent after = e;
		Mapper.Filter filter = mapper.filter();
		if (filter.allow(e)) {
			after = (MessageEvent) mapper.apply(e);
			reporter.addItem(before, after == null ? null : after.toString());
		}
		return after;
	}

	private Timetag timetagOf(File session) {
		return fingerprintOf(session).timetag();
	}

	private Tank<MessageEvent> tankOf(File session) {
		return datalake.messageStore().tank(fingerprintOf(session).tank());
	}

	private Source<MessageEvent> sourceOf(Tank<MessageEvent> tank, File session) {
		Fingerprint fingerprint = fingerprintOf(session);
		return tank.source(fingerprint.source());
	}

	private Collection<File> sessions() {
		backupDirectory.mkdirs();
		return listFiles(backupDirectory, new String[]{"event.session.treated"}, true);
	}

	private void backupSourceSession(Mapper mapper, File session) {
		File dest = new File(session.getParentFile(), mapperPrefixName(mapper) + "_" + session.getName() + ".bak");
		move(session, dest);
	}

	private String mapperPrefixName(Mapper mapper) {
		return "backup_" + mapper.getClass().getSimpleName() + "_" + ts;
	}

	private File temp(File file) {
		return new File(file.getAbsolutePath() + ".tmp");
	}

	private String ts() {
		return Instant.now().toString().replaceAll("[-:]", "").replace("T", "").substring(0, 14);
	}

	private void move(File source, File dest) {
		try {
			Files.move(source.toPath(), dest.toPath());
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private void write(MessageWriter writer, MessageEvent after) {
		try {
			writer.write(after.toMessage());
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private void close(MessageWriter writer) {
		try {
			writer.close();
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private OutputStream zim(File file) {
		try {
			return Zim.compressing(new BufferedOutputStream(new FileOutputStream(file)));
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	private static Fingerprint fingerprintOf(File file) {
		return new Fingerprint(cleanedNameOf(file));
	}

	private static String cleanedNameOf(File file) {
		return file.getName().substring(0, file.getName().indexOf("#"))
				.replace("-", "/")
				.replace(Event.Format.Message + Session.SessionExtension + ".treated", "");
	}
}
