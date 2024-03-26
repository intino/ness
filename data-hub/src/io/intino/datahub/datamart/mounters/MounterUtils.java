package io.intino.datahub.datamart.mounters;

import io.intino.alexandria.event.Event;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.Message;
import io.intino.datahub.datamart.MasterDatamart;
import io.intino.datahub.model.Entity;
import io.intino.datahub.model.Sensor;
import io.intino.datahub.model.Timeline;
import io.intino.datahub.model.Timeline.Raw.Attribute;
import io.intino.magritte.framework.Layer;
import io.intino.sumus.chronos.Magnitude;
import io.intino.sumus.chronos.Period;
import io.intino.sumus.chronos.TimelineStore;
import io.intino.sumus.chronos.TimelineStore.SensorModel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.intino.datahub.box.DataHubBox.TIMELINE_EXTENSION;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class MounterUtils {
	public static Map<String, Set<String>> timelineTypes(MasterDatamart datamart) {
		return datamart.definition().timelineList().stream().collect(Collectors.toMap(Layer::name$, t -> types(t).collect(Collectors.toSet())));
	}

	public static String sourceSensor(Event event) {
		Map<String, String> parameters = parameters(event.ss());
		String sensor = parameters.get("sensor");
		String cleanSS = withOutParameters(event.ss());
		return sensor == null ? cleanSS : sensor;
	}

	public static Magnitude[] sensorModel(SensorModel current, Message assertion, Timeline timeline) {
		Sensor sensor = timeline.asRaw().tank().sensor();
		return sensor.magnitudeList().stream()
				.map(m -> new Magnitude(m.id(), new Magnitude.Model(merge(m, current, assertion, m.attributeList(), timeline.asRaw().attributeList()))))
				.toArray(Magnitude[]::new);
	}

	public static Stream<String> types(Timeline t) {
		return asTypes(tanksOf(t));
	}

	public static Stream<String> tanksOf(Timeline t) {
		Set<String> tanks = new HashSet<>();
		String entityTank = tankName(t.entity());
		if (entityTank != null) tanks.add(entityTank);
		if (t.isRaw()) tanks.add(tankName(t.asRaw().tank().sensor()));
		else tanks.addAll(getCookedTanks(t));
		return tanks.stream();
	}

	public static Set<String> getCookedTanks(Timeline t) {
		Set<String> tanks = new HashSet<>();
		t.asCooked().timeSeriesList().stream().map(ts -> tankName(ts.tank())).forEach(tanks::add);
		t.asCooked().timeSeriesList().stream().filter(Timeline.Cooked.TimeSeries::isCount).flatMap(ts -> tanksOf(ts.asCount())).forEach(tanks::add);
		return tanks;
	}

	public static TimelineStoreBuilder rawTimelineBuilder() {
		return new TimelineStoreBuilder();
	}

	public static File timelineFileOf(File datamartDir, String name, String entity) {
		return new File(datamartDir, name + File.separator + entity + TIMELINE_EXTENSION);
	}

	public static File copyOf(File temp, File file, String newExtension) throws IOException {
		File copy = new File(temp, file.getName() + newExtension);
		if (file.exists()) {
			copy.delete();
			Files.copy(file.toPath(), copy.toPath(), REPLACE_EXISTING);
		}
		return copy;
	}

	public static File tempDir(String prefix) {
		try {
			return Files.createTempDirectory(prefix).toFile();
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	private static String tankName(Entity e) {
		return e.from() == null ? null : e.from().message().core$().fullName().replace("$", ".");
	}

	private static String withOutParameters(String ss) {
		return ss.contains("?") ? ss.substring(0, ss.indexOf("?")) : ss;
	}

	private static Map<String, String> parameters(String ss) {
		int i = ss.indexOf("?");
		if (i < 0 || i == ss.length() - 1) return Map.of();
		String[] parameters = ss.substring(i + 1).split(";");
		return Arrays.stream(parameters).map(p -> p.split("=")).collect(Collectors.toMap(p -> p[0], p -> p[1]));
	}

	private static Stream<String> tanksOf(Timeline.Cooked.TimeSeries.Count ts) {
		return ts.operationList().stream().map(d -> tankName(d.tank()));
	}

	private static Map<String, String> merge(Sensor.Magnitude m, SensorModel current, Message message, List<Sensor.Magnitude.Attribute> magnitudeAttr, List<Attribute> timelineAttr) {
		Map<String, String> attrs = new HashMap<>();
		for (Sensor.Magnitude.Attribute attribute : magnitudeAttr) attrs.put(attribute.name$(), attribute.value());
		if (current != null) {
			Magnitude magnitude = current.get(m.id());
			// TODO: OR check when magnitude == null
			if (magnitude != null) {
				Magnitude.Model model = magnitude.model;
				model.attributes().forEach(a -> attrs.put(a, model.attribute(a)));
			}
		}
		if (message == null) return attrs;
		addMessageAttributes(m, message, timelineAttr, attrs);
		return attrs;
	}

	private static void addMessageAttributes(Sensor.Magnitude m, Message message, List<Attribute> timelineAttr, Map<String, String> attrs) {
		timelineAttr.stream().filter(a -> a.magnitude().equals(m)).forEach(a -> {
			String value = valueOf(message, a.from());
			if (value != null) attrs.put(a.name$(), value);
		});
	}

	private static Stream<String> asTypes(Stream<String> tanks) {
		return tanks.map(t -> t.contains(".") ? t.substring(t.lastIndexOf(".") + 1) : t);
	}

	private static String tankName(Sensor sensor) {
		return sensor.core$().fullName().replace("$", ".");
	}

	private static String tankName(io.intino.datahub.model.Datalake.Tank.Message tank) {
		return tank.message().core$().fullName().replace("$", ".");
	}

	private static String valueOf(Message message, Entity.Attribute source) {
		return message.get(source.name$()).asString();
	}

	public static class TimelineStoreBuilder {
		private File datamartDir;
		private MasterDatamart datamart;
		private Instant start;
		private String type;
		private String entity;
		private String extension;

		public TimelineStore createIfNotExists() throws IOException {
			File file = timelineFileOf(datamartDir, type, entity.replace(":", "-"));
			if (extension != null) file = new File(file.getAbsolutePath() + extension);
			file.getParentFile().mkdirs();
			if (file.exists()) return TimelineStore.of(file);
			return createTimeline(file, definition());
		}

		private TimelineStore createTimeline(File file, Timeline definition) throws IOException {
			return TimelineStore.createIfNotExists(entity, file)
					.withSensorModel(sensorModel(null, datamart.entityStore().get(entity), definition))
					.withTimeModel(start, new Period(definition.asRaw().tank().period(), definition.asRaw().tank().periodScale().chronoUnit()))
					.build();
		}

		private Timeline definition() throws IOException {
			return datamart.definition().timelineList().stream()
					.filter(Timeline::isRaw)
					.filter(t -> t.asRaw().tank().sensor().name$().equals(type))
					.findFirst()
					.orElseThrow(() -> new IOException("Tank not found: " + type));
		}

		public TimelineStoreBuilder withExtension(String extension) {
			this.extension = extension;
			return this;
		}

		public TimelineStoreBuilder datamartDir(File datamartDir) {
			this.datamartDir = datamartDir;
			return this;
		}

		public TimelineStoreBuilder datamart(MasterDatamart datamart) {
			this.datamart = datamart;
			return this;
		}

		public TimelineStoreBuilder start(Instant start) {
			this.start = start;
			return this;
		}

		public TimelineStoreBuilder type(String type) {
			this.type = type;
			return this;
		}

		public TimelineStoreBuilder entity(String entity) {
			this.entity = entity;
			return this;
		}
	}
}