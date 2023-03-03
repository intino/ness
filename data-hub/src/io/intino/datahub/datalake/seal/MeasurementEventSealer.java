package io.intino.datahub.datalake.seal;

import io.intino.alexandria.Fingerprint;
import io.intino.alexandria.datalake.Datalake;
import io.intino.alexandria.datalake.file.FileStore;
import io.intino.alexandria.event.Event;
import io.intino.alexandria.event.EventStream;
import io.intino.alexandria.event.measurement.MeasurementEvent;
import io.intino.alexandria.event.measurement.MeasurementEventWriter;
import io.intino.alexandria.event.message.MessageEvent;
import io.intino.alexandria.event.message.MessageEventReader;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.Message;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

class MeasurementEventSealer {
	private final Datalake datalake;

	MeasurementEventSealer(Datalake datalake) {
		this.datalake = datalake;
	}

	public void seal(Fingerprint fingerprint, List<File> sessions) throws IOException {
		seal(datalakeFile(fingerprint), sessions);
	}

	private void seal(File datalakeFile, List<File> sessions) throws IOException {
		try (MeasurementEventWriter writer = new MeasurementEventWriter(datalakeFile)) {
			writer.write(streamOf(sessions).map(e -> {
				Message message = e.toMessage();
				double[] values = values(message);
				String[] measurements = message.get("measurements").as(String[].class);
				return new MeasurementEvent(e.type(), e.ss(), e.ts(), measurements, values);
			}));
		}
	}

	private static double[] values(Message message) {
		return message.attributes().stream()
				.filter(a -> !a.equals("ts"))
				.filter(a -> !a.equals("sensor"))
				.filter(a -> !a.equals("measurements"))
				.mapToDouble(a -> message.get(a).asDouble()).toArray();
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
