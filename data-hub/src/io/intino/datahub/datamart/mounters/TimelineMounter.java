package io.intino.datahub.datamart.mounters;

import io.intino.alexandria.event.Event;
import io.intino.alexandria.event.measurement.MeasurementEvent;
import io.intino.alexandria.event.message.MessageEvent;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.Message;
import io.intino.datahub.datamart.MasterDatamart;
import io.intino.datahub.model.Entity;
import io.intino.datahub.model.Sensor;
import io.intino.datahub.model.Timeline;
import io.intino.sumus.chronos.Magnitude;
import io.intino.sumus.chronos.Period;
import io.intino.sumus.chronos.TimelineFile;
import io.intino.sumus.chronos.TimelineFile.DataSession;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.intino.datahub.box.DataHubBox.TIMELINE_EXTENSION;

public final class TimelineMounter extends MasterDatamartMounter {

	public TimelineMounter(MasterDatamart datamart) {
		super(datamart);
	}

	@Override
	public void mount(Event event) {
		synchronized (datamart) {
			if (event instanceof MeasurementEvent e) mount(e);
			if (event instanceof MessageEvent e) mount(e.toMessage());
		}
	}

	@Override
	public void mount(Message message) {
		if (message == null) return;
		if (isAssertion(message)) mountAssertion(new MessageEvent(message));
		else mount(measurementEvent(message));
	}

	private boolean isAssertion(Message message) {
		return datamart.definition().timelineList().stream()
				.filter(t -> t.entity().from() != null)
				.anyMatch(t -> t.entity().from().message().name$().equals(message.type()));
	}

	public void mount(MeasurementEvent event) {
		try {
			if (event.ss() == null) return;
			Map<String, String> parameters = parameters(event.ss());
			String sensor = parameters.get("sensor");
			String cleanSS = withOutParameters(event.ss());
			String ss = sensor == null ? cleanSS : sensor;
			TimelineFile timelineFile = datamart.timelineStore().get(event.type(), ss);
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
			if (tlFile.count() == 0 || tlFile.next().isBefore(event.ts()) || Math.abs(Duration.between(event.ts(), tlFile.next()).getSeconds()) / 60 <= 1)
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
		long lapse = Duration.between(tlFile.next(), ts).getSeconds();
		if (lapse > tlFile.period().duration() * 2) session.set(ts);
	}

	private void update(MeasurementEvent event, DataSession session) {
		IntStream.range(0, event.measurements().length).forEach(i -> session.set(event.measurements()[i].name(), event.values()[i]));
	}

	private static MeasurementEvent measurementEvent(Message message) {
		return new MeasurementEvent(message.type(), message.get("ss").asString(), message.get("ts").asInstant(), message.get("measurements").as(String[].class), java.util.Arrays.stream(message.get("values").as(String[].class)).mapToDouble(Double::parseDouble).toArray());
	}

	private TimelineFile createTimelineFile(MeasurementEvent event, String ss) throws IOException {
		File file = new File(box().datamartTimelinesDirectory(datamart.name()), event.type() + File.separator + ss + TIMELINE_EXTENSION);
		file.getParentFile().mkdirs();
		TimelineFile tlFile;
		if (file.exists()) return TimelineFile.open(file);
		tlFile = TimelineFile.create(file, ss);
		Timeline tlDefinition = datamart.definition().timelineList().stream()
				.filter(t -> t.tank().sensor().name$().equals(event.type()))
				.findFirst()
				.orElseThrow(() -> new IOException("Tank not found: " + event.type()));
		tlFile.timeModel(event.ts(), new Period(tlDefinition.tank().period(), tlDefinition.tank().periodScale().chronoUnit()));
		tlFile.sensorModel(sensorModel(datamart.entityStore().get(ss), tlDefinition));
		return tlFile;
	}

	private void mountAssertion(MessageEvent assertion) {
		datamart.definition().timelineList().stream().filter(t -> t.entity().name$().equals(assertion.type())).findFirst().ifPresent(t -> {
			try {
				File file = new File(box().datamartTimelinesDirectory(datamart.name()) + TIMELINE_EXTENSION);
				File tlFile = new File(file, assertion.ss());
				if (!tlFile.exists()) return;
				TimelineFile timelineFile = TimelineFile.open(tlFile);
				timelineFile.sensorModel(sensorModel(assertion.toMessage(), t));
			} catch (IOException e) {
				Logger.error(e);
			}
		});
	}

	private Magnitude[] sensorModel(Message message, Timeline timeline) {
		Sensor sensor = timeline.tank().sensor();
		return sensor.magnitudeList().stream().map(m -> new Magnitude(m.id(), new Magnitude.Model(merge(m, message, m.attributeList(), timeline.attributeList())))).toArray(Magnitude[]::new);
	}

	private Map<String, String> merge(Sensor.Magnitude m, Message message, List<Sensor.Magnitude.Attribute> magnitudeAttr, List<Timeline.Attribute> timelineAttr) {
		Map<String, String> attrs = new HashMap<>();
		for (Sensor.Magnitude.Attribute attribute : magnitudeAttr) attrs.put(attribute.name$(), attribute.value());
		if (message == null) return attrs;
		timelineAttr.stream().filter(a -> a.magnitude().equals(m)).forEach(a -> {
			String value = valueOf(message, a.from());
			if (value != null) attrs.put(a.name$(), value);
		});
		return attrs;
	}

	private String valueOf(Message message, Entity.Attribute source) {
		return message.get(source.name$()).asString();
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
