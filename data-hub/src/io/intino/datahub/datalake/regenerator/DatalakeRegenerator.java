package io.intino.datahub.datalake.regenerator;

import io.intino.alexandria.Timetag;
import io.intino.alexandria.datalake.Datalake;
import io.intino.alexandria.datalake.file.FileDatalake;
import io.intino.alexandria.datalake.file.FileEventTub;
import io.intino.alexandria.datalake.file.eventsourcing.EventPump;
import io.intino.alexandria.datalake.file.eventsourcing.FileEventPump;
import io.intino.alexandria.event.Event;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.MessageWriter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.zip.GZIPOutputStream;

public class DatalakeRegenerator {
	protected final FileDatalake datalake;
	private final File reviewsDirectory;
	private final String ts;

	public DatalakeRegenerator(FileDatalake datalake, File reviewsDirectory, String ts) {
		this.datalake = datalake;
		this.reviewsDirectory = reviewsDirectory;
		this.ts = ts;
	}

	public File review(Mapper mapper) {
		File reportFile = new File(reviewsDirectory, mapperPrefixName(mapper) + ".html");
		RegeneratorReporter reporter = new RegeneratorReporter(reportFile);
		EventPump.Reflow reflow = new FileEventPump(datalake.eventStore()).reflow(mapper.filter());
		while (reflow.hasNext()) reflow.next(1, event -> review(mapper, reporter, event));
		reporter.commit();
		return reportFile;
	}

	public File revise(Mapper mapper) {
		File reportFile = new File(datalake.root(), mapperPrefixName(mapper) + ".html");
		RegeneratorReporter reporter = new RegeneratorReporter(reportFile);
		datalake.eventStore().tanks().sorted(Comparator.comparing(Datalake.EventStore.Tank::name)).filter(tank -> mapper.filter().allow(tank)).forEach(tank -> timetags(tank).forEach(timetag -> {
			if (mapper.filter().allow(tank, timetag)) {
				FileEventTub tub = (FileEventTub) tank.on(timetag);
				if (!tub.file().exists()) return;
				MessageWriter writer = new MessageWriter(this.zipStream(temp(tub)));
				tub.events().forEachRemaining(e -> {
					String before = e.toMessage().toString();
					Event after = e;
					if (mapper.filter().allow(e)) {
						after = mapper.apply(e);
						reporter.addItem(before, after == null ? null : after.toString());
					}
					if (after != null) write(writer, after);
				});
				close(writer);
				backupSourceTub(mapper, tub);
				if (temp(tub).length() > 20) move(temp(tub), tub.file());
				else temp(tub).delete();
			}
		}));
		reporter.commit();
		return reportFile;
	}

	private void close(MessageWriter writer) {
		try {
			writer.close();
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

	private void review(Mapper mapper, RegeneratorReporter reporter, Event event) {
		if (mapper.filter().allow(event)) {
			String before = event.toMessage().toString();
			Event after = mapper.apply(event);
			reporter.addItem(before, after == null ? null : after.toString());
		}
	}

	private File temp(FileEventTub tub) {
		return new File(tub.file().getAbsolutePath() + ".tmp");
	}

	private void backupSourceTub(Mapper mapper, FileEventTub tub) {
		File source = tub.file();
		File dest = new File(source.getParentFile(), mapperPrefixName(mapper) + "_" + source.getName() + ".bak");
		move(source, dest);
	}

	private String mapperPrefixName(Mapper mapper) {
		return "datalake_" + mapper.getClass().getSimpleName() + "_" + ts;
	}


	private Iterable<Timetag> timetags(Datalake.EventStore.Tank tank) {
		return tank.first().timetag().iterateTo(tank.last().timetag());
	}

	private void move(File source, File dest) {
		try {
			Files.move(source.toPath(), dest.toPath());
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
}
