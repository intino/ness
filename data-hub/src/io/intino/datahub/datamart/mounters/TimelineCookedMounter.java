package io.intino.datahub.datamart.mounters;

import io.intino.alexandria.event.message.MessageEvent;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.datamart.MasterDatamart;
import io.intino.datahub.datamart.TimeShiftCache;
import io.intino.datahub.model.Timeline;
import io.intino.datahub.model.Timeline.Cooked;
import io.intino.datahub.model.Timeline.Cooked.TimeSeries;
import io.intino.datahub.model.Timeline.Cooked.TimeSeries.Count.Difference;
import io.intino.datahub.model.Timeline.Cooked.TimeSeries.TimeShift;
import io.intino.magritte.framework.Layer;
import io.intino.sumus.chronos.Magnitude;
import io.intino.sumus.chronos.Magnitude.Model;
import io.intino.sumus.chronos.Period;
import io.intino.sumus.chronos.TimeSeries.Point;
import io.intino.sumus.chronos.TimelineFile;
import io.intino.sumus.chronos.TimelineFile.DataSession;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import static io.intino.datahub.box.DataHubBox.TIMELINE_EXTENSION;
import static io.intino.datahub.datamart.DatamartFactory.tanksOf;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.stream.Collectors.toMap;

public class TimelineCookedMounter {
	private final MasterDatamart datamart;
	private final File directory;
	private final File mounterCacheDirectory;

	public TimelineCookedMounter(DataHubBox box, MasterDatamart datamart) {
		this.datamart = datamart;
		this.directory = box.datamartDirectory(datamart.name());
		this.mounterCacheDirectory = new File(directory, "cache");
	}

	public void mount(MessageEvent event) {
		datamart.definition().timelineList().stream()
				.filter(Timeline::isCooked)
				.map(Timeline::asCooked)
				.filter(t -> types(t).anyMatch(tank -> tank.equals(event.type())))
				.forEach(t -> process(event, t));
	}

	private void process(MessageEvent event, Cooked definition) {
		try {
			TimelineFile timelineFile = getOrCreateTimelineFile(event, definition);
			if (timelineFile == null) return;
			update(timelineFile, definition, event);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private TimelineFile getOrCreateTimelineFile(MessageEvent event, Cooked timelineDef) throws IOException {
		String entityId = entityOf(event, timelineDef);
		if (entityId == null) return null;
		TimelineFile timelineFile = datamart.timelineStore().get(timelineDef.name$(), entityId);
		if (timelineFile == null)
			timelineFile = createTimelineFile(timelineDef, event.ts(), entityId);
		return timelineFile;
	}

	private String entityOf(MessageEvent event, Cooked definition) {
		TimeSeries timeSeries = definition.timeSeries(ts -> ts.tank().message().name$().equals(event.type()));
		if (timeSeries != null) event.toMessage().get(timeSeries.entityId().name$()).asString();
		else {
			for (TimeSeries series : definition.timeSeriesList())
				if (series.isCount()) {
					TimeSeries.Count.Operation operation = series.asCount().operation(o -> o.tank().message().name$().equals(event.type()));
					if (operation != null) return event.toMessage().get(operation.entityId().name$()).asString();
				} else if (series.asTimeShift().withTank().message().name$().equals(event.type()))
					return event.toMessage().get(series.asTimeShift().withEntityId().name$()).asString();
		}
		return null;
	}

	private void update(TimelineFile tlFile, Cooked definition, MessageEvent event) {
		DataSession session = null;
		try {
			session = tlFile.add().set(event.ts());
			List<TimeSeries> timeSeries = timeSeries(definition, event.type());
			for (TimeSeries ts : timeSeries) {
				if (ts.isCount())
					processCount(session, ts.asCount(), lastValue(tlFile, ts), operationOf(ts.asCount().operationList(), event.type()));
				else processTimeShift(session, ts.asTimeShift(), event);
			}
		} catch (IOException e) {
			Logger.error(e);
		} finally {
			close(session);
		}
	}

	private TimeSeries.Count.Operation operationOf(List<TimeSeries.Count.Operation> operations, String type) {
		return operations.stream().filter(o -> o.tank().message().name$().equals(type)).findFirst().orElse(null);
	}

	private void processCount(DataSession session, TimeSeries.Count ts, Point last, TimeSeries.Count.Operation operation) {
		double value = last == null ? 0 : last.value();
		if (operation instanceof Difference) session.set(ts.name$(), value - 1);
		else session.set(ts.name$(), value + 1);
	}

	private void processTimeShift(DataSession session, TimeShift timeSeries, MessageEvent event) {
		if (event.type().equals(timeSeries.tank().message().name$())) save(timeSeries, event);
		else {
			Instant last = load(timeSeries, event);
			if (last != null) session.set(timeSeries.name$(), last.until(event.ts(), SECONDS));
		}
	}

	private void save(TimeShift timeSeries, MessageEvent event) {
		String entity = event.toMessage().get(timeSeries.entityId().name$()).asString();
		try (TimeShiftCache cache = cache(timeSeries)) {
			cache.open();
			cache.put(entity, event.ts());
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	private Instant load(TimeShift timeSeries, MessageEvent event) {
		String entity = event.toMessage().get(timeSeries.entityId().name$()).asString();
		try (TimeShiftCache cache = cache(timeSeries)) {
			cache.open();
			return cache.get(entity);
		} catch (Exception e) {
			Logger.error(e);
			return null;
		}
	}

	private TimeShiftCache cache(TimeShift timeseries) {
		String timeline = timeseries.core$().ownerAs(Timeline.class).name$();
		return new TimeShiftCache(new File(mounterCacheDirectory, timeline + "db"));
	}

	private static Point lastValue(TimelineFile tlFile, TimeSeries ts) throws IOException {
		Point point = tlFile.timeline().get(ts.name$()).last();
		while (point != null && Double.isNaN(point.value())) point = point.prev();
		return point;
	}

	private List<TimeSeries> timeSeries(Cooked definition, String type) {
		return definition.timeSeriesList(ts -> ts.isCount() ? asTypes(tanksOf(ts.asCount())).anyMatch(t -> t.equals(type)) : ts.asTimeShift().tank().message().name$().equals(type));
	}

	private Stream<String> types(Cooked t) {
		return asTypes(tanksOf(t.asTimeline()));
	}

	private Stream<String> asTypes(Stream<String> tanks) {
		return tanks.map(t -> t.contains(".") ? t.substring(t.lastIndexOf(".") + 1) : t);
	}

	private void close(DataSession session) {
		try {
			if (session != null) session.close();
		} catch (IOException ignored) {
		}
	}

	private TimelineFile createTimelineFile(Cooked timeline, Instant start, String entity) throws IOException {
		File file = new File(directory, timeline.name$() + File.separator + entity + TIMELINE_EXTENSION);
		file.getParentFile().mkdirs();
		TimelineFile tlFile;
		if (file.exists()) return TimelineFile.open(file);
		tlFile = TimelineFile.create(file, entity).timeModel(start, new Period(1, HOURS)).sensorModel(sensorModel(timeline));
		return tlFile;
	}

	static Magnitude[] sensorModel(Cooked timeline) {
		return timeline.timeSeriesList().stream()
				.map(ts -> new Magnitude(ts.name$(), new Model(ts.attributeList().stream().collect(toMap(Layer::name$, TimeSeries.Attribute::value)))))
				.toArray(Magnitude[]::new);
	}
}