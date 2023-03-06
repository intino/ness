package io.intino.datahub.datalake.seal;

import io.intino.alexandria.Fingerprint;
import io.intino.alexandria.datalake.Datalake;
import io.intino.alexandria.datalake.file.FS;
import io.intino.alexandria.event.Event.Format;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.sealing.EventSealer;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static io.intino.alexandria.Session.SessionExtension;
import static io.intino.alexandria.event.Event.Format.*;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;

public class EventSessionSealer {
	private final Datalake datalake;
	private final io.intino.datahub.model.Datalake graphDl;
	private final File stageDir;
	private final File tmpDir;
	private final File treatedDir;
	private EventSealer messageSealer;
	private MeasurementEventSealer measurementSealer;

	public EventSessionSealer(Datalake datalake, io.intino.datahub.model.Datalake graphDl, File stageDir, File tmpDir, File treatedDir) {
		this.datalake = datalake;
		this.graphDl = graphDl;
		this.stageDir = stageDir;
		this.tmpDir = tmpDir;
		this.treatedDir = treatedDir;
	}

	public void seal() {
		seal(t -> true);
	}

	public void seal(Predicate<String> sorting) {
		messageSealer = new EventSealer(datalake, sorting, tmpDir);
		measurementSealer = new MeasurementEventSealer(datalake, graphDl);
		sessions(stageDir).collect(groupingBy(EventSessionSealer::fingerprintOf)).entrySet()
				.stream().sorted(comparing(t -> t.getKey().toString()))
				.parallel()
				.forEach(this::seal);
	}

	private void seal(Map.Entry<Fingerprint, List<File>> e) {
		try {
			Format format = formatOf(e.getKey().tank());
			if (format == Message) messageSealer.seal(e.getKey(), e.getValue());
			else if (format == Measurement) measurementSealer.seal(e.getKey(), e.getValue());
			moveTreated(e);
		} catch (IOException ex) {
			Logger.error(ex);
		}
	}

	private Format formatOf(String tankName) {
		return graphDl.tankList().stream()
				.filter(tank -> matches(tankName, tank))
				.findFirst()
				.map(tank -> (tank.isMessage() ? Message : Measurement)).orElse(Unknown);
	}

	private static boolean matches(String tankName, io.intino.datahub.model.Datalake.Tank tank) {
		if (tank.isMessage() && tank.asMessage().qn().equals(tankName)) return true;
		return tank.isMeasurement() && tank.asMeasurement().qn().equals(tankName);
	}

	private void moveTreated(Map.Entry<Fingerprint, List<File>> e) {
		e.getValue().forEach(f -> f.renameTo(new File(treatedDir, f.getName() + ".treated")));
	}

	private static Stream<File> sessions(File stage) {
		if (!stage.exists()) return Stream.empty();
		return FS.allFilesIn(stage, f -> f.getName().endsWith(SessionExtension) && f.length() > 0f);
	}

	private static Fingerprint fingerprintOf(File file) {
		return Fingerprint.of(file);
	}

}
