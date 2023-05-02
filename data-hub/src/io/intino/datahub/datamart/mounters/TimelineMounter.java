package io.intino.datahub.datamart.mounters;

import io.intino.alexandria.event.measurement.MeasurementEvent;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.Message;
import io.intino.datahub.datamart.MasterDatamart;
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
		try {
			String ss = message.get("ss").asString();
			if (ss == null) return;

			TimelineFile timelineFile = datamart.timelineStore().get(ss);
			if (timelineFile == null) timelineFile = createTimelineFile(ss);
			update(timelineFile, message);
		} catch (Exception e) {
			Logger.error("Could not mount message " + message + ": " + e.getMessage(), e);
		}
	}

	private void update(TimelineFile timelineFile, Message message) {
		append(timelineFile, measurementEvent(message));
	}

	private void append(TimelineFile tlFile, MeasurementEvent event) {
		DataSession session = null;
		try {
			session = tlFile.add();
			checkTs(event.ts(), tlFile, session);
			if (tlFile.next().isBefore(event.ts()) || Math.abs(Duration.between(event.ts(), tlFile.next()).getSeconds()) / 60 <= 1)
				append(event, session);
		} catch (IOException e) {
			Logger.error(e);
		} finally {
			close(session);
		}
	}

	private void close(DataSession session) {
		if(session == null) return;
		try {
			session.close();
		} catch (IOException ignored) {}
	}

	private static void checkTs(Instant ts, TimelineFile tlFile, DataSession session) throws IOException {
		long lapse = Duration.between(ts, tlFile.next()).getSeconds();
		if (lapse > tlFile.period().duration() * 2) session.set(ts);
	}

	private void append(MeasurementEvent event, DataSession session) {
		IntStream.range(0, event.measurements().length).forEach(i -> session.set(event.measurements()[i].name(), event.values()[i]));
	}

	private static MeasurementEvent measurementEvent(Message message) {
		return new MeasurementEvent(message.type(), message.get("ss").asString(), message.get("ts").asInstant(), message.get("measurements").as(String[].class), java.util.Arrays.stream(message.get("values").as(String[].class)).mapToDouble(Double::parseDouble).toArray());
	}

	private TimelineFile createTimelineFile(String ss) throws IOException {
		return TimelineFile.create(new File(box().datamartTimelinesDirectory(datamart.name()), ss + TIMELINE_EXTENSION), ss);
	}
}
