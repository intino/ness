package systems.intino.eventsourcing.datahub.datalake.seal;

import io.intino.alexandria.FS;
import io.intino.alexandria.logger.Logger;
import systems.intino.eventsourcing.datalake.Datalake;
import systems.intino.eventsourcing.sealing.EventSealer;
import systems.intino.eventsourcing.sealing.SessionSealer;
import systems.intino.eventsourcing.session.Fingerprint;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static systems.intino.eventsourcing.session.Session.SessionExtension;

public class EventSessionSealer {
	private final Datalake datalake;
	private final systems.intino.eventsourcing.datahub.model.Datalake graphDl;
	private final File stageDir;
	private final File tmpDir;
	private final File treatedDir;
	private EventSealer eventSealer;

	public EventSessionSealer(Datalake datalake, systems.intino.eventsourcing.datahub.model.Datalake graphDl, File stageDir, File tmpDir, File treatedDir) {
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
		sessions(stageDir).collect(groupingBy(EventSessionSealer::fingerprintOf)).entrySet()
				.stream().sorted(comparing(t -> t.getKey().toString()))
				.parallel()
				.forEach(this::seal);
	}

	private void seal(Map.Entry<systems.intino.eventsourcing.session.Fingerprint, List<File>> e) {
		try {
			eventSealer.seal(e.getKey(), e.getValue());
			moveTreated(e);
		} catch (IOException ex) {
			Logger.error(ex);
		}
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
