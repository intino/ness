package io.intino.datahub.datamart.mounters;

import io.intino.alexandria.event.Event;
import io.intino.alexandria.event.measurement.MeasurementEvent;
import io.intino.alexandria.event.measurement.MeasurementEvent.Magnitude;
import io.intino.alexandria.event.measurement.MeasurementEvent.Magnitude.Attribute;
import io.intino.alexandria.event.message.MessageEvent;
import io.intino.alexandria.message.Message;
import io.intino.datahub.datamart.MasterDatamart;
import io.intino.datahub.model.Timeline;
import io.intino.magritte.framework.Layer;
import io.intino.sumus.chronos.TimelineFile;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.intino.alexandria.event.measurement.MeasurementEvent.ATTRIBUTE_SEP;
import static io.intino.alexandria.event.measurement.MeasurementEvent.NAME_VALUE_SEP;
import static io.intino.datahub.datamart.mounters.TimelineUtils.timelineFileOf;
import static io.intino.datahub.datamart.mounters.TimelineUtils.types;
import static java.util.Arrays.stream;

public class TimelineMounter extends MasterDatamartMounter {
	private final TimelineRawMounter rawMounter;
	private final TimelineAssertionMounter assertionMounter;
	private final TimelineCookedMounter cookedMounter;
	private final Map<String, List<String>> timelineTypes;

	public TimelineMounter(MasterDatamart datamart) {
		super(datamart);
		timelineTypes = datamart.definition().timelineList().stream().collect(Collectors.toMap(Layer::name$, t -> types(t).toList()));
		rawMounter = rawMounter(datamart);
		assertionMounter = new TimelineAssertionMounter(box(), datamart);
		cookedMounter = new TimelineCookedMounter(box(), datamart, timelineTypes);
	}

	protected TimelineRawMounter rawMounter(MasterDatamart datamart) {
		return new TimelineRawMounter(box(), datamart);
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
		else if (hasValues(message)) rawMounter.mount((measurementEvent(message)));
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
		private final TimelineAssertionMounter assertionMounter;

		private TimelineFile tlFile;
		private final TimelineFileFactory timelineFactory;

		public OfSingleTimeline(MasterDatamart datamart, String tank, String ss) {
			File file = timelineFileOf(datamart.box().datamartTimelinesDirectory(datamart.name()), tank, ss);
			if(file.exists()) file.delete();
			else file.getParentFile().mkdirs();
			this.timelineFactory = e -> TimelineUtils.createTimelineFileOfRawTimeline(datamart.box().datamartTimelinesDirectory(datamart.name()), datamart, e.ts(), tank, ss);
			this.rawMounter = new TimelineRawMounter.OfSingleTimeline(datamart.box(), datamart, this::getTimelineFile);
			this.assertionMounter = new TimelineAssertionMounter.OfSingleTimeline(datamart.box(), datamart, this::getTimelineFile);
		}

		public void mount(Event event) {
			if(tlFile == null) createTimelineFile(event);
			if (event instanceof MeasurementEvent e) rawMounter.mount(e);
			else if (event instanceof MessageEvent e)  {
				rawMounter.close();
				assertionMounter.mount(new MessageEvent(e.toMessage()));
			}
		}

		private void createTimelineFile(Event event) {
			try {
				this.tlFile = timelineFactory.createFromFirstEvent(event);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void close() throws Exception {
			rawMounter.close();
		}

		private TimelineFile getTimelineFile() {
			return tlFile;
		}

		private interface TimelineFileFactory {
			TimelineFile createFromFirstEvent(Event event) throws Exception;
		}
	}
}