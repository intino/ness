package io.intino.datahub.datalake.seal;

import io.intino.alexandria.Fingerprint;
import io.intino.alexandria.datalake.Datalake;
import io.intino.alexandria.datalake.file.FileStore;
import io.intino.alexandria.event.Event;
import io.intino.alexandria.event.EventStream;
import io.intino.alexandria.event.measurement.MeasurementEvent;
import io.intino.alexandria.event.message.MessageEvent;
import io.intino.alexandria.event.message.MessageEventReader;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.Message;
import io.intino.alexandria.zit.Zit;
import io.intino.alexandria.zit.ZitWriter;
import io.intino.alexandria.zit.model.Period;
import io.intino.datahub.model.Sensor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

class MeasurementEventSealer {
	private final Datalake datalake;
	private final io.intino.datahub.model.Datalake dlDefinition;

	MeasurementEventSealer(Datalake datalake, io.intino.datahub.model.Datalake dlDefinition) {
		this.datalake = datalake;
		this.dlDefinition = dlDefinition;
	}

	public void seal(Fingerprint fingerprint, List<File> sessions) throws IOException {
		File dlFile = datalakeFile(fingerprint);
		File session = new File(dlFile.getAbsolutePath() + ".session");
		Stream<MeasurementEvent> eventStream = streamOf(sessions)
				.map(e -> new MeasurementEvent(e.type(), e.ss(), e.ts(), magnitudes(e), values(e.toMessage())));
		try (ZitWriter writer = initFile(fingerprint, session)) {
			if (writer == null) return;
			final String[][] magnitudes = {null};
			EventStream.merge(Stream.of(EventStream.of(dlFile), eventStream))
					.forEach(m -> {
						updateMagnitudes(m, magnitudes, writer);
						writer.put(m.ts(), m.values());
					});
		}
		Files.move(session.toPath(), dlFile.toPath(), ATOMIC_MOVE, REPLACE_EXISTING);
	}

	private static void updateMagnitudes(MeasurementEvent m, String[][] magnitudes, ZitWriter writer) {
		String[] array = Arrays.stream(m.magnitudes()).map(MeasurementEvent.Magnitude::toString).toArray(String[]::new);
		if (!Arrays.equals(magnitudes[0], array)) {
			writer.put(array);
			magnitudes[0] = array;
		}
	}

	private static String[] magnitudes(MessageEvent e) {
		return e.toMessage().contains("magnitudes") ? e.toMessage().get("magnitudes").as(String[].class) : e.toMessage().get("measurements").as(String[].class);
	}

	private ZitWriter initFile(Fingerprint fingerprint, File datalakeFile) {
		String tankName = fingerprint.tank();
		io.intino.datahub.model.Datalake.Tank tank = dlDefinition.tank(t -> t.qn().equals(tankName));
		if (tank == null) return null;
		try {
			return new ZitWriter(datalakeFile,
					tank.asMeasurement().sensor().name$(),
					fingerprint.source(),
					Period.of(tank.asMeasurement().period(), tank.asMeasurement().periodScale().chronoUnit()),
					sensorModel(tank));
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	private static String[] sensorModel(io.intino.datahub.model.Datalake.Tank tank) {
		return tank.asMeasurement().sensor().magnitudeList().stream()
				.map(m -> m.id() + (m.attributeList().isEmpty() ? "" : Zit.ATTRIBUTE_DELIMITER + toString(m.attributeList()))).
				toArray(String[]::new);
	}

	private static String toString(List<Sensor.Magnitude.Attribute> attrs) {
		return attrs.stream().map(a -> a.name$() + "=" + a.value()).collect(Collectors.joining(Zit.ATTRIBUTE_DELIMITER));
	}

	private static double[] values(Message message) {
		return Arrays.stream(message.get("values").as(String[].class)).mapToDouble(((Double::parseDouble))).toArray();
	}

	private Stream<MessageEvent> streamOf(List<File> files) throws IOException {
		if (files.size() == 1) return new EventStream<>(new MessageEventReader(files.get(0))).sorted();
		return EventStream.merge(files.stream().map(file -> {
			try {
				return new EventStream<>((new MessageEventReader(files.get(0)))).sorted();
			} catch (IOException e) {
				Logger.error(e);
				return Stream.empty();
			}
		}));
	}

	private File datalakeFile(Fingerprint fingerprint) {
		File zimFile = new File(((FileStore) datalake.measurementStore()).directory(), fingerprint.tank() + File.separator + fingerprint.source() + File.separator + fingerprint.timetag() + Event.Format.Measurement.extension());
		zimFile.getParentFile().mkdirs();
		return zimFile;
	}

}
