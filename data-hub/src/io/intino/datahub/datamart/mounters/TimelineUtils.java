package io.intino.datahub.datamart.mounters;

import io.intino.alexandria.message.Message;
import io.intino.datahub.datamart.MasterDatamart;
import io.intino.datahub.model.Entity;
import io.intino.datahub.model.Sensor;
import io.intino.datahub.model.Timeline;
import io.intino.datahub.model.Timeline.Raw.Attribute;
import io.intino.sumus.chronos.Magnitude;
import io.intino.sumus.chronos.Period;
import io.intino.sumus.chronos.TimelineFile;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

import static io.intino.datahub.box.DataHubBox.TIMELINE_EXTENSION;

public class TimelineUtils {


	static Magnitude[] sensorModel(Message entity, Timeline timeline) {
		Sensor sensor = timeline.asRaw().tank().sensor();
		return sensor.magnitudeList().stream()
				.map(m -> new Magnitude(m.id(), new Magnitude.Model(merge(m, entity, m.attributeList(), timeline.asRaw().attributeList()))))
				.toArray(Magnitude[]::new);
	}

	private static Map<String, String> merge(Sensor.Magnitude m, Message message, List<Sensor.Magnitude.Attribute> magnitudeAttr, List<Attribute> timelineAttr) {
		Map<String, String> attrs = new HashMap<>();
		for (Sensor.Magnitude.Attribute attribute : magnitudeAttr) attrs.put(attribute.name$(), attribute.value());
		if (message == null) return attrs;
		timelineAttr.stream().filter(a -> a.magnitude().equals(m)).forEach(a -> {
			String value = valueOf(message, a.from());
			if (value != null) attrs.put(a.name$(), value);
		});
		return attrs;
	}


	public static Stream<String> types(Timeline t) {
		return asTypes(tanksOf(t));
	}

	public static Stream<String> tanksOf(Timeline t) {
		Set<String> tanks = new HashSet<>();
		String entityTank = tankName(t.entity());
		if (entityTank != null) tanks.add(entityTank);
		if (t.isRaw()) tanks.add(tankName(t.asRaw().tank().sensor()));
		else {
			tanks.addAll(t.asCooked().timeSeriesList().stream().map(ts -> tankName(ts.tank())).toList());
			tanks.addAll(t.asCooked().timeSeriesList().stream().filter(Timeline.Cooked.TimeSeries::isCount).flatMap(ts -> tanksOf(ts.asCount())).toList());
		}
		return tanks.stream();
	}

	private static Stream<String> asTypes(Stream<String> tanks) {
		return tanks.map(t -> t.contains(".") ? t.substring(t.lastIndexOf(".") + 1) : t);
	}

	public static Stream<String> tanksOf(Timeline.Cooked.TimeSeries.Count ts) {
		return ts.operationList().stream().map(d -> tankName(d.tank()));
	}

	private static String tankName(Sensor sensor) {
		return sensor.core$().fullName().replace("$", ".");
	}

	private static String tankName(io.intino.datahub.model.Datalake.Tank.Message tank) {
		return tank.message().core$().fullName().replace("$", ".");
	}


	private static String tankName(Entity e) {
		return e.from() == null ? null : e.from().message().core$().fullName().replace("$", ".");
	}

	static TimelineFile createTimelineFile(File datamartDir, MasterDatamart datamart, Instant start, String name, String entity) throws IOException {
		File file = new File(datamartDir, name + File.separator + entity + TIMELINE_EXTENSION);
		file.getParentFile().mkdirs();
		TimelineFile tlFile;
		if (file.exists()) return TimelineFile.open(file);
		tlFile = TimelineFile.create(file, entity);
		Timeline tlDefinition = datamart.definition().timelineList().stream()
				.filter(t -> t.asRaw().tank().sensor().name$().equals(name))
				.findFirst()
				.orElseThrow(() -> new IOException("Tank not found: " + name));
		tlFile.timeModel(start, new Period(tlDefinition.asRaw().tank().period(), tlDefinition.asRaw().tank().periodScale().chronoUnit()));
		tlFile.sensorModel(sensorModel(datamart.entityStore().get(entity), tlDefinition));
		return tlFile;
	}


	private static String valueOf(Message message, Entity.Attribute source) {
		return message.get(source.name$()).asString();
	}

}
