package io.intino.datahub.datamart.mounters;

import io.intino.alexandria.event.Event;
import io.intino.alexandria.event.measurement.MeasurementEvent;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.datamart.MasterDatamart;
import io.intino.sumus.chronos.TimelineFile;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.intino.datahub.datamart.mounters.TimelineUtils.createTimelineFile;

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
			String ss = sourceSensor(event);
			TimelineFile timelineFile = getOrCreate(event, ss);
			update(timelineFile, event);
		} catch (Exception e) {
			Logger.error("Could not mount event " + event.type() + ", ss = " + event.ss() + ": " + e.getMessage(), e);
		}
	}

	private TimelineFile getOrCreate(MeasurementEvent event, String ss) throws IOException {
		TimelineFile timelineFile = datamart.timelineStore().get(event.type(), ss);
		if (timelineFile == null)
			timelineFile = createTimelineFile(box.datamartTimelinesDirectory(datamart.name()), datamart, event.ts(), event.type(), ss);
		return timelineFile;
	}

	private void update(TimelineFile tlFile, MeasurementEvent event) {
		TimelineFile.DataSession session = null;
		try {
			session = tlFile.add();
			checkTs(event.ts(), tlFile, session);
			if (tlFile.count() == 0 || tlFile.next().isBefore(event.ts()) || Math.abs(Duration.between(event.ts(), tlFile.next()).getSeconds()) / 60 <= 1)
				update(event, session);
		} catch (IOException e) {
			Logger.error(e);
		} finally {
			close(session);
		}
	}

	private static void checkTs(Instant ts, TimelineFile tlFile, TimelineFile.DataSession session) throws IOException {
		long lapse = Duration.between(tlFile.next(), ts).getSeconds();
		if (lapse > tlFile.period().duration() * 2) session.set(ts);
	}

	private void update(MeasurementEvent event, TimelineFile.DataSession session) {
		IntStream.range(0, event.measurements().length).forEach(i -> session.set(event.measurements()[i].name(), event.values()[i]));
	}

	private void close(TimelineFile.DataSession session) {
		try {
			if (session == null) return;
			session.close();
		} catch (IOException ignored) {
		}
	}


	private String sourceSensor(Event event) {
		Map<String, String> parameters = parameters(event.ss());
		String sensor = parameters.get("sensor");
		String cleanSS = withOutParameters(event.ss());
		return sensor == null ? cleanSS : sensor;
	}

	private String withOutParameters(String ss) {
		return ss.contains("?") ? ss.substring(0, ss.indexOf("?")) : ss;
	}

	private Map<String, String> parameters(String ss) {
		int i = ss.indexOf("?");
		if (i < 0 || i == ss.length() - 1) return Map.of();
		String[] parameters = ss.substring(i + 1).split(";");
		return Arrays.stream(parameters).map(p -> p.split("=")).collect(Collectors.toMap(p -> p[0], p -> p[1]));
	}
}
