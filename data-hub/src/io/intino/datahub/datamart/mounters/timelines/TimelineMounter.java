package io.intino.datahub.datamart.mounters.timelines;

import io.intino.alexandria.event.Event;
import io.intino.alexandria.event.measurement.MeasurementEvent;
import io.intino.alexandria.event.measurement.MeasurementEvent.Magnitude;
import io.intino.alexandria.event.measurement.MeasurementEvent.Magnitude.Attribute;
import io.intino.alexandria.event.message.MessageEvent;
import io.intino.alexandria.message.Message;
import io.intino.datahub.datamart.MasterDatamart;
import io.intino.datahub.datamart.mounters.MasterDatamartMounter;
import io.intino.datahub.datamart.mounters.MounterUtils;
import io.intino.datahub.model.Timeline;
import io.intino.sumus.chronos.TimelineStore;
import io.intino.sumus.chronos.timelines.TimelineWriter;
import io.intino.sumus.chronos.timelines.stores.FileTimelineStore;

import java.io.File;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.intino.alexandria.event.measurement.MeasurementEvent.ATTRIBUTE_SEP;
import static io.intino.alexandria.event.measurement.MeasurementEvent.NAME_VALUE_SEP;
import static io.intino.datahub.datamart.mounters.MounterUtils.sourceSensor;
import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;

public class TimelineMounter extends MasterDatamartMounter {
	private final TimelineRawMounter rawMounter;
	private final TimelineAssertionMounter assertionMounter;
	private final TimelineCookedMounter cookedMounter;
	private final Map<String, Set<String>> timelineTypes;

	public TimelineMounter(MasterDatamart datamart) {
		super(datamart);
		timelineTypes = MounterUtils.timelineTypes(datamart);
		rawMounter = new TimelineRawMounter(box(), datamart, timelineTypes);
		assertionMounter = new TimelineAssertionMounter(box(), datamart);
		cookedMounter = new TimelineCookedMounter(box(), datamart, timelineTypes);
	}

	@Override
	public void mount(Event event) {
		synchronized (datamart) {
			if (event instanceof MeasurementEvent e) rawMounter.mount(e);
			else if (event instanceof MessageEvent e) mount(e.toMessage());
		}
	}

	@Override
	public void mount(Message message) {
		if (message == null) return;
		if (isAssertion(message)) assertionMounter.mount(new MessageEvent(message));
		else if (isCooked(message)) cookedMounter.mount(new MessageEvent(message));
		else if (hasValues(message)) rawMounter.mount(measurementEvent(message));
	}

	@Override
	public List<String> destinationsOf(Message message) {
		if (message == null || isAssertion(message)) return emptyList();
		else if (isCooked(message)) return cookedMounter.destinationsOf(new MessageEvent(message));
		else if (hasValues(message)) return rawMounter.destinationsOf(measurementEvent(message));
		return emptyList();
	}


	private boolean hasValues(Message message) {
		return message.contains("values");
	}

	private boolean isAssertion(Message message) {
		return datamart.definition().timelineList().stream()
				.filter(t -> t.entity().from() != null)
				.anyMatch(t -> t.entity().from().message().name$().equals(message.type()));
	}

	private boolean isCooked(Message message) {
		return datamart.definition().timelineList().stream()
				.filter(Timeline::isCooked)
				.anyMatch(t -> timelineTypes.get(t.name$()).contains(message.type()));
	}

	private static MeasurementEvent measurementEvent(Message message) {
		return new MeasurementEvent(message.type(),
				message.get("ss").asString(),
				message.get("ts").asInstant(),
				measurements(message),
				values(message));
	}

	private static Magnitude[] measurements(Message message) {
		List<String> measurements = message.get("magnitudes").asList(String.class);
		return measurements.stream()
				.map(TimelineMounter::magnitude)
				.toArray(Magnitude[]::new);
	}

	private static Magnitude magnitude(String m) {
		String[] fields = m.split(ATTRIBUTE_SEP);
		return new Magnitude(fields[0], attributes(fields));
	}

	private static Attribute[] attributes(String[] m) {
		return Arrays.stream(m)
				.skip(1)
				.map(a -> new Attribute(a.split(NAME_VALUE_SEP)))
				.toArray(Attribute[]::new);
	}

	private static double[] values(Message message) {
		return stream(message.get("values").as(String[].class)).mapToDouble(Double::parseDouble).toArray();
	}

	public static class OfSingleTimeline implements AutoCloseable {
		private final TimelineRawMounter.OfSingleTimeline rawMounter;
		private final TimelineAssertionMounter.OfSingleTimeline assertionMounter;
		private final String ss;
		private final TimelineFileFactory timelineFactory;
		private TimelineWriter writer;
		private File sessionFile;

		public OfSingleTimeline(MasterDatamart datamart, Timeline timeline, String tank, String ss) {
			this.ss = ss;
			this.timelineFactory = ts -> MounterUtils.rawTimelineBuilder()
					.datamart(datamart)
					.datamartDir(datamart.box().datamartTimelinesDirectory(datamart.name()))
					.type(tank)
					.entity(ss)
					.start(ts)
					.withExtension(".session")
					.createIfNotExists();
			this.rawMounter = new TimelineRawMounter.OfSingleTimeline(datamart, this::getTimelineWriter);
			this.assertionMounter = new TimelineAssertionMounter.OfSingleTimeline(datamart, timeline, this::getTimelineWriter);
		}

		public void mount(Event event) {
			if (event instanceof MeasurementEvent e) {
				createTimelineFileIfNotExists(event.ts());
				rawMounter.mount(e);
			} else if (event instanceof MessageEvent e && ss.equals(sourceSensor(event)))
				assertionMounter.mount(new MessageEvent(e.toMessage()));
		}

		private void createTimelineFileIfNotExists(Instant ts) {
			if (writer != null) return;
			try {
				TimelineStore store = timelineFactory.create(ts);
				writer = store.writer();
				sessionFile = ((FileTimelineStore) store).file();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void close() throws Exception {
			if (writer != null) {
				writer.close();
				File timelineFile = new File(sessionFile.getAbsolutePath().replace(".session", ""));
				Files.move(sessionFile.toPath(), timelineFile.toPath(), REPLACE_EXISTING, ATOMIC_MOVE);
			}
		}

		private TimelineWriter getTimelineWriter() {
			return writer;
		}

		private interface TimelineFileFactory {
			TimelineStore create(Instant firstTS) throws Exception;
		}
	}
}