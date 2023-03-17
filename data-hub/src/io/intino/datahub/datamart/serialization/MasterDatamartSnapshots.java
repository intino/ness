package io.intino.datahub.datamart.serialization;

import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.datamart.MasterDatamart;
import io.intino.datahub.datamart.MasterDatamart.Snapshot;
import io.intino.datahub.model.Datamart;
import io.intino.datahub.model.NessGraph;

import java.io.*;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.reverseOrder;

public class MasterDatamartSnapshots {

	public static final String SNAPSHOT_EXTENSION = ".datamart.snapshot";

	public static void saveSnapshot(File datamartsRoot, Timetag timetag, MasterDatamart<?> datamart) throws IOException {
		File file = snapshotDirOf(datamartsRoot, datamart.name() + "/" + timetag.value() + SNAPSHOT_EXTENSION);
		file.getParentFile().mkdirs();
		MasterDatamartSerializer.serialize(datamart, new FileOutputStream(file));
	}

	public static List<Timetag> listAvailableSnapshotsOf(File datamartsRoot, String datamartName) {
		return listSnapshotFilesIn(snapshotDirOf(datamartsRoot, datamartName)).stream()
				.map(MasterDatamartSnapshots::timetagOf)
				.collect(Collectors.toList());
	}

	public static <T> Optional<Snapshot<T>> loadMostRecentSnapshot(File datamartsRoot, String datamartName, NessGraph graph) {
		return loadMostRecentSnapshotTo(datamartsRoot, datamartName, Timetag.of(LocalDate.now(), Scale.Day), graph);
	}

	public static <T> Optional<Snapshot<T>> loadMostRecentSnapshotTo(File datamartsRoot, String datamartName, Timetag timetag, NessGraph graph) {
		return findSnapshotFileOf(snapshotDirOf(datamartsRoot, datamartName), timetag).map(f -> deserialize(f, datamartName, graph));
	}

	private static <T> Snapshot<T> deserialize(File file, String datamartName, NessGraph graph) {
		try {
			MasterDatamart<T> datamart = MasterDatamartSerializer.deserialize(new FileInputStream(file), definitionOf(datamartName, graph));
			return new Snapshot<>(timetagOf(file), datamart);
		} catch (IOException e) {
			Logger.error("Failed to deserialize datamart snapshot " + file.getName() + ": " + e.getMessage(), e);
			return null;
		}
	}

	private static Datamart definitionOf(String name, NessGraph graph) {
		return graph.datamartList().stream()
				.filter(d -> d.name$().equals(name))
				.findFirst().orElseThrow(() -> new IllegalArgumentException("No datamart named " + name + " defined"));
	}

	private static Timetag timetagOf(File file) {
		try {
			String name = file.getName().replace(SNAPSHOT_EXTENSION, "");
			Timetag timetag = Timetag.of(name.substring(name.indexOf('.') + 1));
			timetag.datetime();
			return timetag;
		} catch (Exception ignored) {
			return null;
		}
	}

	private static File snapshotDirOf(File datamartsRoot, String datamartName) {
		return new File(datamartsRoot, datamartName);
	}

	private static Optional<File> findSnapshotFileOf(File dir, Timetag timetag) {
		return listSnapshotFilesIn(dir).stream()
				.sorted(reverseOrder())
				.filter(f -> snapshotIsEqualOrBefore(timetagOf(f), timetag)).findFirst();
	}

	private static List<File> listSnapshotFilesIn(File dir) {
		File[] files = dir.listFiles(f -> f.getName().endsWith(SNAPSHOT_EXTENSION) && timetagOf(f) != null);
		if(files == null || files.length == 0) return emptyList();
		return Arrays.asList(files);
	}

	private static boolean snapshotIsEqualOrBefore(Timetag snapshotTimetag, Timetag targetTimetag) {
		return snapshotTimetag.equals(targetTimetag) || snapshotTimetag.isBefore(targetTimetag);
	}
}
