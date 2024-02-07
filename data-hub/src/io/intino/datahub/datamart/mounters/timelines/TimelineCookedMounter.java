package io.intino.datahub.datamart.mounters.timelines;

import io.intino.alexandria.event.message.MessageEvent;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.datamart.MasterDatamart;
import io.intino.datahub.datamart.TimeShiftCache;
import io.intino.datahub.model.Timeline;
import io.intino.datahub.model.Timeline.Cooked;
import io.intino.datahub.model.Timeline.Cooked.TimeSeries;
import io.intino.datahub.model.Timeline.Cooked.TimeSeries.Count.Difference;
import io.intino.datahub.model.Timeline.Cooked.TimeSeries.Count.Operation;
import io.intino.datahub.model.Timeline.Cooked.TimeSeries.TimeShift;
import io.intino.magritte.framework.Layer;
import io.intino.sumus.chronos.Magnitude;
import io.intino.sumus.chronos.Magnitude.Model;
import io.intino.sumus.chronos.MeasurementsVector;
import io.intino.sumus.chronos.Period;
import io.intino.sumus.chronos.TimeSeries.Point;
import io.intino.sumus.chronos.TimelineStore;
import io.intino.sumus.chronos.timelines.TimelineWriter;
import io.intino.sumus.chronos.timelines.stores.FileTimelineStore;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.intino.datahub.box.DataHubBox.TIMELINE_EXTENSION;
import static io.intino.datahub.datamart.MasterDatamart.normalizePath;
import static io.intino.datahub.datamart.mounters.MounterUtils.copyOf;
import static io.intino.datahub.datamart.mounters.MounterUtils.tempDir;
import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.stream.Collectors.toMap;

public class TimelineCookedMounter {
	private final MasterDatamart datamart;
	private final Map<String, Set<String>> timelineTypes;
	private final File tlDirectory;
	private final IndicatorMounter indicatorMounter;
	private final File temp;

	public TimelineCookedMounter(DataHubBox box, MasterDatamart datamart, Map<String, Set<String>> timelineTypes) {
		this.datamart = datamart;
		this.timelineTypes = timelineTypes;
		this.tlDirectory = box.datamartTimelinesDirectory(datamart.name());
		this.indicatorMounter = new IndicatorMounter(datamart);
		temp = tempDir("reel_mounter");

	}

	public void mount(MessageEvent event) {
		datamart.definition().timelineList().stream()
				.filter(Timeline::isCooked)
				.map(Timeline::asCooked)
				.filter(t -> timelineTypes.getOrDefault(t.name$(), Set.of()).contains(event.type()))
				.forEach(t -> mount(event, t));
	}

	public List<String> destinationsOf(MessageEvent event) {
		return datamart.definition().timelineList().stream()
				.filter(Timeline::isCooked)
				.map(Timeline::asCooked)
				.filter(t -> timelineTypes.getOrDefault(t.name$(), Set.of()).contains(event.type()))
				.map(t -> t.name$() + "\0" + entityOf(event, t))
				.toList();
	}

	protected void mount(MessageEvent event, Cooked timelineDefinition) {
		String entityId = entityOf(event, timelineDefinition);
		if (entityId == null) return;
		TimelineStore timelineStore = updateTimeline(event, timelineDefinition, entityId);
		if (timelineStore != null && timelineDefinition.asTimeline().isIndicator())
			indicatorMounter.mount(timelineDefinition.name$(), timelineStore);
	}

	protected TimelineStore updateTimeline(MessageEvent event, Cooked definition, String entityId) {
		try {
			TimelineStore store = getOrCreateTimelineStore(event, definition, entityId);
			if (store == null) return null;
			updateTimeline(store, definition, event);
			return store;
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	private TimelineStore getOrCreateTimelineStore(MessageEvent event, Cooked timelineDef, String entityId) throws IOException {
		TimelineStore timelineFile = datamart.timelineStore().get(timelineDef.name$(), entityId);
		return timelineFile == null ? createTimelineStore(timelineDef, event.ts(), entityId) : timelineFile;
	}

	private static String entityOf(MessageEvent event, Cooked definition) {
		TimeSeries timeSeries = definition.timeSeries(ts -> ts.tank().message().name$().equals(event.type()));
		if (timeSeries != null) return event.toMessage().get(timeSeries.entityId().name$()).asString();
		for (TimeSeries series : definition.timeSeriesList())
			if (series.isCount()) {
				Operation operation = series.asCount().operation(o -> o.tank().message().name$().equals(event.type()));
				if (operation != null) return event.toMessage().get(operation.entityId().name$()).asString();
			} else if (series.asTimeShift().withTank().message().name$().equals(event.type()))
				return event.toMessage().get(series.asTimeShift().withEntityId().name$()).asString();
		return null;
	}

	private void updateTimeline(TimelineStore tlStore, Cooked definition, MessageEvent event) {
		File timelineFile = ((FileTimelineStore) tlStore).file();
		File sessionFile = null;
		try {
			io.intino.sumus.chronos.Timeline timeline = tlStore.timeline();
			sessionFile = copyOf(temp, timelineFile, ".session");
			try (TimelineWriter writer = TimelineStore.of(sessionFile).writer()) {
				MeasurementsVector vector = createVector(tlStore.sensorModel());
				writer.set(event.ts());
				for (TimeSeries ts : timeSeries(definition, event.type()))
					fillMeasurements(tlStore, vector, event, ts);
				fillNaNValues(tlStore.sensorModel(), vector, timeline);
				writer.set(vector);
			}
			Files.move(sessionFile.toPath(), timelineFile.toPath(), REPLACE_EXISTING, ATOMIC_MOVE);
		} catch (IOException e) {
			Logger.error(e);
			if (sessionFile != null) sessionFile.delete();
		}
	}


	private static MeasurementsVector createVector(TimelineStore.SensorModel sensorModel) {
		MeasurementsVector measurements = new MeasurementsVector(sensorModel);
		sensorModel.forEach(m -> measurements.set(m.label, Double.NaN));
		return measurements;
	}

	private static void fillNaNValues(TimelineStore.SensorModel sensorModel, MeasurementsVector measurements, io.intino.sumus.chronos.Timeline timeline) {
		sensorModel.forEach(m -> {
			if (Double.isNaN(measurements.toArray()[measurements.sensorModel().indexOf(m.label)])) {
				Point last = timeline.get(m.label).last();
				if (last != null) measurements.set(m.label, last.value());
			}
		});
	}

	private void fillMeasurements(TimelineStore tlFile, MeasurementsVector vector, MessageEvent event, TimeSeries ts) throws IOException {
		if (ts.isCount())
			processCount(vector, ts.asCount(), lastValue(tlFile, ts), operationOf(ts.asCount().operationList(), event.type()));
		else if (ts.isTimeShift()) processTimeShift(vector, ts.asTimeShift(), event);
	}

	private Operation operationOf(List<Operation> operations, String type) {
		return operations.stream().filter(o -> o.tank().message().name$().equals(type)).findFirst().orElse(null);
	}

	private void processCount(MeasurementsVector measurements, TimeSeries.Count ts, Point last, Operation operation) {
		double value = last == null ? 0 : last.value();
		if (operation instanceof Difference) measurements.set(ts.name$(), Math.max(0, value - 1));
		else measurements.set(ts.name$(), value + 1);
	}

	private void processTimeShift(MeasurementsVector measurements, TimeShift timeSeries, MessageEvent event) {
		if (event.type().equals(timeSeries.tank().message().name$())) save(timeSeries, event);
		else {
			Instant last = load(timeSeries, event);
			if (last != null) measurements.set(timeSeries.name$(), last.until(event.ts(), SECONDS));
		}
	}

	private void save(TimeShift timeSeries, MessageEvent event) {
		TimeShiftCache cache = cache(timeSeries);
		cache.put(event.toMessage().get(timeSeries.entityId().name$()).asString(), event.ts());
	}

	private Instant load(TimeShift timeSeries, MessageEvent event) {
		return cache(timeSeries).get(event.toMessage().get(timeSeries.entityId().name$()).asString());
	}

	private TimeShiftCache cache(TimeShift timeseries) {
		return datamart.cacheOf(timeseries.core$().ownerAs(Timeline.class).name$());
	}

	private static Point lastValue(TimelineStore tlFile, TimeSeries ts) throws IOException {
		Point point = tlFile.timeline().get(ts.name$()).last();
		while (point != null && Double.isNaN(point.value())) point = point.prev();
		return point;
	}

	private List<TimeSeries> timeSeries(Cooked definition, String type) {
		return definition.timeSeriesList(ts -> timelineTypes.get(definition.name$()).contains(type));
	}

	private TimelineStore createTimelineStore(Cooked timeline, Instant start, String entity) throws IOException {
		File file = new File(tlDirectory, normalizePath(timeline.name$() + File.separator + entity + TIMELINE_EXTENSION));
		file.getParentFile().mkdirs();
		return TimelineStore.createIfNotExists(entity, file)
				.withTimeModel(start, new Period(1, SECONDS))
				.withSensorModel(sensorModel(timeline))
				.build();
	}

	static Magnitude[] sensorModel(Cooked timeline) {
		return timeline.timeSeriesList().stream()
				.map(ts -> new Magnitude(ts.name$(), new Model(ts.attributeList().stream().collect(toMap(Layer::name$, TimeSeries.Attribute::value)))))
				.toArray(Magnitude[]::new);
	}


	public static class OneShot extends TimelineCookedMounter {

		public OneShot(DataHubBox box, MasterDatamart datamart, Map<String, Set<String>> timelineTypes) {
			super(box, datamart, timelineTypes);
		}

		protected void mount(MessageEvent event, Cooked timelineDefinition) {
			String entityId = entityOf(event, timelineDefinition);
			if (entityId != null) updateTimeline(event, timelineDefinition, entityId);
		}

	}
}