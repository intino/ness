package io.intino.datahub.datamart.mounters;

import io.intino.alexandria.event.measurement.MeasurementEvent;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.Message;
import io.intino.datahub.datamart.MasterDatamart;
import io.intino.datahub.model.Timeline;
import io.intino.sumus.chronos.Period;
import io.intino.sumus.chronos.TimelineFile;
import io.intino.sumus.chronos.TimelineFile.DataSession;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.stream.IntStream;

import static io.intino.datahub.box.DataHubBox.TIMELINE_EXTENSION;

public final class TimelineMounter extends MasterDatamartMounter {

	public TimelineMounter(MasterDatamart datamart) {
		super(datamart);
	}

	@Override
	public void mount(Message message) {
		if (message == null) return;
		mount(measurementEvent(message));
	}

	public void mount(MeasurementEvent event) {
		try {
			if (event.ss() == null) return;
			String ss = withOutParameters(event.ss());
			TimelineFile timelineFile = datamart.timelineStore().get(ss);
			if (timelineFile == null) timelineFile = createTimelineFile(event, ss);
			update(timelineFile, event);
		} catch (Exception e) {
			Logger.error("Could not mount event " + event.type() + ", ss = " + event.ss() + ": " + e.getMessage(), e);
		}
	}

	private void update(TimelineFile tlFile, MeasurementEvent event) {
		DataSession session = null;
		try {
			session = tlFile.add();
			checkTs(event.ts(), tlFile, session);
			if (tlFile.next().isBefore(event.ts()) || Math.abs(Duration.between(event.ts(), tlFile.next()).getSeconds()) / 60 <= 1)
				update(event, session);
		} catch (IOException e) {
			Logger.error(e);
		} finally {
			close(session);
		}
	}

	private void close(DataSession session) {
		if (session == null) return;
		try {
			session.close();
		} catch (IOException ignored) {
		}
	}

	private static void checkTs(Instant ts, TimelineFile tlFile, DataSession session) throws IOException {
		long lapse = Duration.between(ts, tlFile.next()).getSeconds();
		if (lapse > tlFile.period().duration() * 2) session.set(ts);
	}

	private void update(MeasurementEvent event, DataSession session) {
		IntStream.range(0, event.measurements().length).forEach(i -> session.set(event.measurements()[i].name(), event.values()[i]));
	}

	private static MeasurementEvent measurementEvent(Message message) {
		return new MeasurementEvent(message.type(), message.get("ss").asString(), message.get("ts").asInstant(), message.get("measurements").as(String[].class), java.util.Arrays.stream(message.get("values").as(String[].class)).mapToDouble(Double::parseDouble).toArray());
	}

	private TimelineFile createTimelineFile(MeasurementEvent event, String ss) throws IOException {
		File file = new File(box().datamartTimelinesDirectory(datamart.name()), ss + TIMELINE_EXTENSION);
		file.getParentFile().mkdirs();
		TimelineFile timelineFile = TimelineFile.create(file, ss);
		Timeline timeline = datamart.definition().timelineList().stream()
				.filter(t -> t.tank().sensor().name$().equals(event.type()))
				.findFirst()
				.orElseThrow(() -> new IOException("Tank not found"));
		timelineFile.timeModel(event.ts(), new Period(timeline.tank().period(), timeline.tank().periodScale().chronoUnit()));
		return timelineFile;
	}

	private String withOutParameters(String ss) {
		return ss.contains("?") ? ss.substring(0, ss.indexOf("?")) : ss;
	}
}
