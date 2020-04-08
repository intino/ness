package io.intino.datahub.datalake.regenerator;

import io.intino.alexandria.Fingerprint;
import io.intino.alexandria.Session;
import io.intino.alexandria.Timetag;
import io.intino.alexandria.datalake.Datalake;
import io.intino.alexandria.datalake.Datalake.EventStore.Tank;
import io.intino.alexandria.event.Event;
import io.intino.alexandria.event.EventReader;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.MessageWriter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Collection;
import java.util.zip.GZIPOutputStream;

import static org.apache.commons.io.FileUtils.listFiles;

public class SessionRegenerator {

	private final Datalake datalake;
	private final File backupDirectory;
	private final File reviewDirectory;
	private final String ts;

	public SessionRegenerator(Datalake datalake, File backupDirectory, File reviewDirectory, String ts) {
		this.datalake = datalake;
		this.backupDirectory = backupDirectory;
		this.reviewDirectory = reviewDirectory;
		this.ts = ts();
	}

	public File review(Mapper mapper) {
		File reportFile = new File(reviewDirectory, mapperPrefixName(mapper) + ".html");
		RegeneratorReporter reporter = new RegeneratorReporter(reportFile);
		for (File session : sessions()) {
			if (!suitable(session, mapper.filter())) continue;
			new EventReader(session).forEachRemaining(e -> {
				String before = e.toString();
				map(mapper, reporter, session, e, before);
			});
		}
		reporter.commit();
		return reportFile;
	}

	public File revise(Mapper mapper) {
		File reportFile = new File(backupDirectory, mapperPrefixName(mapper) + ".html");
		RegeneratorReporter reporter = new RegeneratorReporter(reportFile);
		for (File session : sessions()) {
			if (!suitable(session, mapper.filter())) continue;
			MessageWriter writer = new MessageWriter(this.zipStream(temp(session)));
			new EventReader(session).forEachRemaining(e -> {
				String before = e.toString();
				Event after = map(mapper, reporter, session, e, before);
				if (after != null) write(writer, after);
			});
			close(writer);
			backupSourceSession(mapper, session);
			if (temp(session).length() > 20) move(temp(session), session);
			else temp(session).delete();
		}
		reporter.commit();
		return reportFile;
	}

	private boolean suitable(File session, Mapper.Filter filter) {
		Tank tank = tankOf(session);
		return filter.allow(tank) && filter.allow(tank, timetagOf(session));
	}

	private Event map(Mapper mapper, RegeneratorReporter reporter, File session, Event e, String before) {
		Event after = e;
		Mapper.Filter filter = mapper.filter();
		if (filter.allow(e)) {
			after = mapper.apply(e);
			reporter.addItem(before, after == null ? null : after.toString());
		}
		return after;
	}

	private Timetag timetagOf(File session) {
		return fingerprintOf(session).timetag();
	}

	private Tank tankOf(File session) {
		return datalake.eventStore().tank(fingerprintOf(session).tank());
	}

	private Collection<File> sessions() {
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
		return Instant.now().toString().replaceAll("-|:", "").replace("T", "").substring(0, 14);
	}

	private void move(File source, File dest) {
		try {
			Files.move(source.toPath(), dest.toPath());
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private void write(MessageWriter writer, Event after) {
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

	private GZIPOutputStream zipStream(File file) {
		try {
			return new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	private static Fingerprint fingerprintOf(File file) {
		return new Fingerprint(cleanedNameOf(file));
	}

	private static String cleanedNameOf(File file) {
		return file.getName().substring(0, file.getName().indexOf("#")).replace("-", "/").replace(Session.EventSessionExtension + ".treated", "");
	}
}
