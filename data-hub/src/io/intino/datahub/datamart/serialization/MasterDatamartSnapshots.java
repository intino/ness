package io.intino.datahub.datamart.serialization;

import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.datamart.MasterDatamart;
import io.intino.datahub.datamart.MasterDatamart.Snapshot;
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

	public static void saveSnapshot(File datamartsRoot, Timetag timetag, MasterDatamart<?> datamart) throws IOException {
		File file = snapshotDirOf(datamartsRoot, datamart.name() + "/" + timetag.value() + ".datamart.snapshot");
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
		return findSnapshotFileOf(snapshotDirOf(datamartsRoot, datamartName), timetag).map(f -> deserialize(f, graph));
	}

	private static <T> Snapshot<T> deserialize(File file, NessGraph graph) {
		try {
			MasterDatamart<T> datamart = MasterDatamartSerializer.deserialize(new BufferedReader(new FileReader(file)), graph);
			return new Snapshot<>(timetagOf(file), datamart);
		} catch (IOException e) {
			Logger.error("Failed to deserialize datamart snapshot " + file.getName() + ": " + e.getMessage(), e);
			return null;
		}
	}

	private static Timetag timetagOf(File file) {
		String name = file.getName();
		return Timetag.of(name.substring(name.indexOf('.')));
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
		File[] files = dir.listFiles(f -> f.getName().endsWith(".datamart.snapshot"));
		if(files == null || files.length == 0) return emptyList();
		return Arrays.asList(files);
	}

	private static boolean snapshotIsEqualOrBefore(Timetag snapshotTimetag, Timetag targetTimetag) {
		return snapshotTimetag.equals(targetTimetag) || snapshotTimetag.isBefore(targetTimetag);
	}
}
