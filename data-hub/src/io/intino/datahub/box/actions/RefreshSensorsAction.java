package io.intino.datahub.box.actions;

import io.intino.alexandria.datalake.file.FileDatalake;
import io.intino.alexandria.datalake.file.FileStore;
import io.intino.alexandria.datalake.file.measurement.MeasurementEventTank;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.model.Datalake;
import io.intino.datahub.model.NessGraph;
import io.intino.datahub.model.Sensor;
import io.intino.datahub.model.Sensor.Magnitude.Attribute;
import io.intino.magritte.framework.Layer;
import io.intino.sumus.chronos.Magnitude;
import io.intino.sumus.chronos.Magnitude.Model;
import io.intino.sumus.chronos.TimelineFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RefreshSensorsAction {

	private final FileDatalake datalake;
	private final NessGraph graph;

	public RefreshSensorsAction(NessGraph graph, FileDatalake datalake) {
		this.graph = graph;
		this.datalake = datalake;
	}


	public void execute() {
		String extension = ((FileStore) datalake.measurementStore()).fileExtension();
		graph.datalake().tankList(Datalake.Tank::isMeasurement).stream().map(Datalake.Tank::asMeasurement).forEach(t -> {
			MeasurementEventTank tank = (MeasurementEventTank) datalake.measurementStore().tank(t.qn());
			try {
				Files.walk(tank.root().toPath()).map(Path::toFile).filter(f -> f.isFile() && f.getName().endsWith(extension)).parallel().forEach(f -> {
					try {
						TimelineFile open = TimelineFile.open(f);
						open.sensorModel(mergeSensorModel(open.sensorModel(), t.sensor()));
					} catch (IOException e) {
						Logger.error(e);
					}
				});
			} catch (IOException e) {
				Logger.error(e);
			}
		});
	}

	private Magnitude[] mergeSensorModel(TimelineFile.SensorModel current, Sensor s) {
		Map<String, Magnitude> fromModel = s.magnitudeList().stream().map(m -> new Magnitude(m.id(), new Model(m.attributeList().stream().collect(Collectors.toMap(Layer::name$, Attribute::value))))).collect(Collectors.toMap(m -> m.label, m -> m));
		for (Magnitude magnitude : current)
			if (fromModel.containsKey(magnitude.label))
				fromModel.put(magnitude.label, merge(fromModel.get(magnitude.label), magnitude));
		return fromModel.values().toArray(new Magnitude[0]);
	}

	private Magnitude merge(Magnitude magnitude, Magnitude old) {
		Map<String, String> attrs = mapOf(old);
		attrs.putAll(mapOf(magnitude));
		return new Magnitude(magnitude.label, new Model(attrs));
	}

	private Map<String, String> mapOf(Magnitude old) {
		return old.model.attributes().stream().collect(Collectors.toMap(k -> k, old.model::attribute));
	}

	private static String[] toString(List<Attribute> attributes) {
		return attributes.stream()
				.map(a -> a.name$() + "=" + a.value())
				.toArray(String[]::new);
	}
}
