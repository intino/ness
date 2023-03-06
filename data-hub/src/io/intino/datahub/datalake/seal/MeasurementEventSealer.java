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
import io.intino.alexandria.zit.ZitWriter;
import io.intino.alexandria.zit.model.Period;
import io.intino.magritte.framework.Layer;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

class MeasurementEventSealer {
	private final Datalake datalake;
	private final io.intino.datahub.model.Datalake dlDefinition;

	MeasurementEventSealer(Datalake datalake, io.intino.datahub.model.Datalake dlDefinition) {
		this.datalake = datalake;
		this.dlDefinition = dlDefinition;
	}

	public void seal(Fingerprint fingerprint, List<File> sessions) throws IOException {
		File file = datalakeFile(fingerprint);
		try (ZitWriter writer = file.exists() ? new ZitWriter(file) : initFile(fingerprint, file)) {
			if (writer == null) return;
			streamOf(sessions)
					.map(e -> new MeasurementEvent(e.type(), e.ss(), e.ts(), e.toMessage().get("measurements").as(String[].class), values(e.toMessage())))
					.forEach(m -> writer.put(m.ts(), m.values()));
		}
	}

	private ZitWriter initFile(Fingerprint fingerprint, File datalakeFile) {
		String tankName = fingerprint.tank();
		io.intino.datahub.model.Datalake.Tank tank = dlDefinition.tank(t -> t.qn().equals(tankName));
		if (tank == null) return null;
		try {
			return new ZitWriter(datalakeFile,
					fingerprint.source(),
					Period.of(tank.asMeasurement().period(), tank.asMeasurement().periodScale().chronoUnit()),
					tank.asMeasurement().measurement().valueList().stream().map(Layer::name$).toArray(String[]::new));
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	private static double[] values(Message message) {
		return Arrays.stream(message.get("values").as(String[].class)).mapToDouble(((Double::parseDouble))).toArray();
	}

	private Stream<MessageEvent> streamOf(List<File> files) throws IOException {
		if (files.size() == 1) return new EventStream<>(new MessageEventReader(files.get(0)));
		return EventStream.merge(files.stream().map(file -> {
			try {
				return new EventStream<>((new MessageEventReader(files.get(0))));
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
