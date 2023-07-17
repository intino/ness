package io.intino.datahub.datalake.seal;

import io.intino.alexandria.FS;
import io.intino.alexandria.Fingerprint;
import io.intino.alexandria.datalake.Datalake;
import io.intino.alexandria.event.Event.Format;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.sealing.EventSealer;
import io.intino.alexandria.sealing.SessionSealer;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
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
	private EventSealer eventSealer;
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

	public void seal(SessionSealer.TankNameFilter tankNameFilter) {
		eventSealer = new EventSealer(datalake, tankNameFilter, tmpDir);
		measurementSealer = new MeasurementEventSealer(datalake, graphDl);
		sessions(stageDir).collect(groupingBy(EventSessionSealer::fingerprintOf)).entrySet()
				.stream().sorted(comparing(t -> t.getKey().toString()))
				.parallel()
				.forEach(this::seal);
	}

	private void seal(Map.Entry<Fingerprint, List<File>> e) {
		try {
			switch (formatOf(e.getKey().tank())) {
				case Message, Resource -> eventSealer.seal(e.getKey(), e.getValue());
				case Measurement -> measurementSealer.seal(e.getKey(), e.getValue());
			}
			moveTreated(e);
		} catch (IOException ex) {
			Logger.error(ex);
		}
	}

	private Format formatOf(String tankName) {
		return graphDl.tankList().stream()
				.filter(tank -> matches(tankName, tank))
				.findFirst()
				.map(this::formatOf).orElse(Unknown);
	}

	private Format formatOf(io.intino.datahub.model.Datalake.Tank tank) {
		if (tank.isMessage()) return Message;
		if (tank.isMeasurement()) return Measurement;
		if (tank.isResource()) return Resource;
		return Unknown;
	}

	private static boolean matches(String tankName, io.intino.datahub.model.Datalake.Tank tank) {
		if (tank.isMessage() && tank.asMessage().qn().equals(tankName)) return true;
		if (tank.isMeasurement() && tank.asMeasurement().qn().equals(tankName)) return true;
		return tank.isResource() && tank.asResource().qn().equals(tankName);
	}

	private void moveTreated(Map.Entry<Fingerprint, List<File>> e) {
		e.getValue().forEach(f -> f.renameTo(new File(treatedDir, f.getName() + ".treated")));
	}

	private static Stream<File> sessions(File stage) {
		if (!stage.exists()) return Stream.empty();
		try {
			return FS.allFilesIn(stage, f -> f.getName().endsWith(SessionExtension) && f.length() > 0L);
		} catch (IOException e) {
			Logger.error("Error while listing sessions in " + stage + ": " + e.getMessage(), e);
			return Stream.empty();
		}
	}

	private static Fingerprint fingerprintOf(File file) {
		return Fingerprint.of(file);
	}
}
