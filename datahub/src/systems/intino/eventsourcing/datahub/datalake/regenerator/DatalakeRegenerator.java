package systems.intino.eventsourcing.datahub.datalake.regenerator;

import io.intino.alexandria.logger.Logger;
import systems.intino.eventsourcing.datahub.datalake.pump.EventPump;
import systems.intino.eventsourcing.datahub.datalake.pump.FileEventPump;
import systems.intino.eventsourcing.datahub.datalake.regenerator.Mapper.Filter;
import systems.intino.eventsourcing.datalake.Datalake;
import systems.intino.eventsourcing.datalake.Datalake.Store.Source;
import systems.intino.eventsourcing.datalake.FileTub;
import systems.intino.eventsourcing.datalake.file.FileDatalake;
import systems.intino.eventsourcing.event.Event;
import systems.intino.eventsourcing.event.message.MessageEvent;
import systems.intino.eventsourcing.message.MessageWriter;
import systems.intino.eventsourcing.zim.Zim;

import java.io.*;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.stream.Stream;

import static systems.intino.eventsourcing.datalake.Datalake.Store.Tank;
import static systems.intino.eventsourcing.datalake.Datalake.Store.Tub;

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
		EventPump.Reflow reflow = new FileEventPump(datalake.messageStore()).reflow(mapper.filter());
		while (reflow.hasNext()) reflow.next(1, event -> review(mapper, reporter, (MessageEvent) event));
		reporter.commit();
		return reportFile;
	}

	public File revise(Mapper mapper) {
		File reportFile = new File(datalake.root(), mapperPrefixName(mapper) + ".html");
		RegeneratorReporter reporter = new RegeneratorReporter(reportFile);
		datalake.messageStore().tanks()
				.sorted(Comparator.comparing(Datalake.Store.Tank::name))
				.filter(tank -> mapper.filter().allow(tank))
				.flatMap(tank -> tubs(tank, tank.sources(), mapper.filter()))
				.forEach(tub -> {
					MessageWriter writer = new MessageWriter(zim(temp(tub)));
					tub.events().forEach(e -> {
						String before = e.toMessage().toString();
						MessageEvent after = e;
						if (mapper.filter().allow(e)) {
							after = (MessageEvent) mapper.apply(e);
							reporter.addItem(before, after == null ? null : after.toString());
						}
						if (after != null) write(writer, after);
					});
					close(writer);
					backupSourceTub(mapper, tub);
					if (temp(tub).length() > 20) move(temp(tub), ((FileTub) tub).file());
					else temp(tub).delete();
				});
		reporter.commit();
		return reportFile;
	}

	private Stream<Tub<MessageEvent>> tubs(Tank<MessageEvent> tank, Stream<Source<MessageEvent>> sources, Filter filter) {
		return sources.flatMap(s -> s.tubs().filter(t -> filter.allow(tank, s, t.timetag())));
	}

	private void close(MessageWriter writer) {
		try {
			writer.close();
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

	private void review(Mapper mapper, RegeneratorReporter reporter, MessageEvent event) {
		if (mapper.filter().allow(event)) {
			String before = event.toMessage().toString();
			Event after = mapper.apply(event);
			reporter.addItem(before, after == null ? null : after.toString());
		}
	}

	private File temp(Datalake.Store.Tub<MessageEvent> tub) {
		return new File(((FileTub) tub).file().getAbsolutePath() + ".tmp");
	}

	private void backupSourceTub(Mapper mapper, Datalake.Store.Tub<MessageEvent> tub) {
		File source = ((FileTub) tub).file();
		File dest = new File(source.getParentFile(), mapperPrefixName(mapper) + "_" + source.getName() + ".bak");
		move(source, dest);
	}

	private String mapperPrefixName(Mapper mapper) {
		return "datalake_" + mapper.getClass().getSimpleName() + "_" + ts;
	}

	private void move(File source, File dest) {
		try {
			Files.move(source.toPath(), dest.toPath());
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
}
