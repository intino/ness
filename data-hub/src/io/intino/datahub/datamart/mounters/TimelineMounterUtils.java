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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.intino.datahub.box.DataHubBox.TIMELINE_EXTENSION;

public class TimelineMounterUtils {


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
