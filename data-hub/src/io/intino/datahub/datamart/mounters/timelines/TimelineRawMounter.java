package io.intino.datahub.datamart.mounters.timelines;

import io.intino.alexandria.event.measurement.MeasurementEvent;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.datamart.MasterDatamart;
import io.intino.sumus.chronos.TimelineStore;
import io.intino.sumus.chronos.timelines.TimelineWriter;
import io.intino.sumus.chronos.timelines.stores.FileTimelineStore;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

import static io.intino.datahub.datamart.mounters.MounterUtils.*;
import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class TimelineRawMounter {
	private final DataHubBox box;
	private final MasterDatamart datamart;

	public TimelineRawMounter(DataHubBox box, MasterDatamart datamart) {
		this.box = box;
		this.datamart = datamart;
	}

	public void mount(MeasurementEvent event) {
		try {
			if (event.ss() == null) return;
			TimelineStore store = getOrCreateTimelineStore(event, sourceSensor(event));
			update(store, event);
		} catch (Exception e) {
			Logger.error("Could not mount event " + event.type() + ", ss = " + event.ss() + ": " + e.getMessage(), e);
		}
	}

	public List<String> destinationsOf(MeasurementEvent event) {
		return List.of(event.type() + "\0" + sourceSensor(event));
	}

	private TimelineStore getOrCreateTimelineStore(MeasurementEvent event, String sensor) throws IOException {
		return rawTimelineBuilder()
				.datamart(datamart)
				.datamartDir(box.datamartTimelinesDirectory(datamart.name()))
				.start(event.ts())
				.type(event.type())
				.entity(sensor)
				.createIfNotExists();
	}

	protected void update(TimelineStore tlStore, MeasurementEvent event) throws IOException {
		File timelineFile = ((FileTimelineStore) tlStore).file();
		File sessionFile = copyOf(timelineFile, ".session");
		try {
			try (TimelineWriter writer = TimelineStore.of(sessionFile).writer()) {
				checkTs(event.ts(), writer);
				// measurements must be present in sensorModel and in the order defined by the sensorModel
				writer.set(event.values());
			}
			Files.move(sessionFile.toPath(), timelineFile.toPath(), REPLACE_EXISTING, ATOMIC_MOVE);
		} catch (IOException e) {
			sessionFile.delete();
			throw e;
		}
	}

	private static void checkTs(Instant ts, TimelineWriter writer) throws IOException {
		long lapse = Duration.between(writer.header().next(), ts).getSeconds();
		if (lapse > writer.timeModel().period().duration() * 2) writer.set(ts);
	}

	private static String name(MeasurementEvent event, int i) {
		//FIXME remove when all measurement events removed
		String name = event.magnitudes()[i].name();
		return name.contains("=") ? name.substring(0, name.indexOf(":")) : name;
	}

	public static class OfSingleTimeline extends TimelineRawMounter {

		private final Supplier<TimelineWriter> writer;

		public OfSingleTimeline(MasterDatamart datamart, Supplier<TimelineWriter> writer) {
			super(datamart.box(), datamart);
			this.writer = writer;
		}

		@Override
		public void mount(MeasurementEvent event) {
			try {
				TimelineWriter writer = this.writer.get();
				checkTs(event.ts(), writer);
				writer.set(event.values()); // TODO: measurements must be present in sensorModel and in the order defined by the sensorModel
			} catch (Exception e) {
				Logger.error("Could not mount event " + event.type() + ", ss = " + event.ss() + ": " + e.getMessage(), e);
			}
		}
	}
}