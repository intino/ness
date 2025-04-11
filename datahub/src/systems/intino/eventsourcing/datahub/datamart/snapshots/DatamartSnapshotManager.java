package systems.intino.eventsourcing.datahub.datamart.snapshots;

import io.intino.alexandria.Timetag;
import systems.intino.eventsourcing.datahub.box.DatahubBox;
import systems.intino.eventsourcing.datahub.datamart.MasterDatamart;
import systems.intino.eventsourcing.datahub.model.Datamart;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.reverseOrder;

public class DatamartSnapshotManager {
	public static final String SNAPSHOT_EXTENSION = ".dm-snapshot.zim";

	private final DatahubBox box;

	public DatamartSnapshotManager(DatahubBox box) {
		this.box = box;
	}

	public void saveSnapshot(Timetag timetag, MasterDatamart datamart) {
		File file = snapshotDirOf(datamart.name() + "/" + timetag.value() + SNAPSHOT_EXTENSION);
		file.getParentFile().mkdirs();
		//TODO
	}

	public List<Timetag> listAvailableSnapshotsOf(String datamartName) {
		return listSnapshotFilesIn(snapshotDirOf(datamartName)).stream()
				.map(this::timetagOf)
				.collect(Collectors.toList());
	}


	private Datamart definitionOf(String name) {
		return box.graph().datamartList().stream()
				.filter(d -> d.name$().equals(name))
				.findFirst().orElseThrow(() -> new IllegalArgumentException("No datamart named " + name + " defined"));
	}

	public File snapshotDirOf(String datamartName) {
		return new File(box.datamartsDirectory(), datamartName);
	}

	private Optional<File> findSnapshotFileOf(File dir, Timetag timetag) {
		return listSnapshotFilesIn(dir).stream()
				.sorted(reverseOrder())
				.filter(f -> snapshotIsEqualOrBefore(timetagOf(f), timetag)).findFirst();
	}

	private Timetag timetagOf(File file) {
		try {
			String name = file.getName().replace(SNAPSHOT_EXTENSION, "");
			Timetag timetag = Timetag.of(name.substring(name.indexOf('.') + 1));
			timetag.datetime();
			return timetag;
		} catch (Exception ignored) {
			return null;
		}
	}

	private List<File> listSnapshotFilesIn(File dir) {
		File[] files = dir.listFiles(f -> f.getName().endsWith(SNAPSHOT_EXTENSION) && timetagOf(f) != null);
		if (files == null || files.length == 0) return emptyList();
		return Arrays.asList(files);
	}

	private boolean snapshotIsEqualOrBefore(Timetag snapshotTimetag, Timetag targetTimetag) {
		if (snapshotTimetag == null) return false; // TODO: check
		return snapshotTimetag.equals(targetTimetag) || snapshotTimetag.isBefore(targetTimetag);
	}
}
