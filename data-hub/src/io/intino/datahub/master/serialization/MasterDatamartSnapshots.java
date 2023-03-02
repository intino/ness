package io.intino.datahub.master.serialization;

import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.master.MasterDatamart;
import io.intino.datahub.master.MasterDatamart.Snapshot;

import java.io.*;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static java.util.Collections.reverseOrder;

public class MasterDatamartSnapshots {

	public static void save(File datamartsRoot, Timetag timetag, MasterDatamart<?> datamart) throws IOException {
		File file = snapshotDirOf(datamartsRoot, datamart.name() + "/" + timetag.value() + ".datamart.snapshot");
		file.getParentFile().mkdirs();
		try(BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			MasterDatamartSerializer.serialize(datamart, writer);
		}
	}

	public static <T> Optional<Snapshot<T>> loadMostRecentSnapshot(File datamartsRoot, String datamartName) {
		return loadMostRecentSnapshotTo(datamartsRoot, datamartName, Timetag.of(LocalDate.now(), Scale.Day));
	}

	public static <T> Optional<Snapshot<T>> loadMostRecentSnapshotTo(File datamartsRoot, String datamartName, Timetag timetag) {
		return findSnapshotFileOf(snapshotDirOf(datamartsRoot, datamartName), timetag).map(MasterDatamartSnapshots::deserialize);
	}

	private static <T> Snapshot<T> deserialize(File file) {
		try {
			MasterDatamart<T> datamart = MasterDatamartSerializer.deserialize(new BufferedReader(new FileReader(file)));
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

	private static Optional<File> findSnapshotFileOf(File file, Timetag timetag) {
		File[] files = file.listFiles(f -> f.getName().endsWith(".datamart.snapshot"));
		if(files == null || files.length == 0) return Optional.empty();
		return Arrays.stream(files)
				.sorted(reverseOrder())
				.filter(f -> snapshotIsEqualOrBefore(timetagOf(f), timetag)).findFirst();
	}

	private static boolean snapshotIsEqualOrBefore(Timetag snapshotTimetag, Timetag targetTimetag) {
		return snapshotTimetag.equals(targetTimetag) || snapshotTimetag.isBefore(targetTimetag);
	}
}
