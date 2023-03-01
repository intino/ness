package io.intino.datahub.master.serialization;

import io.intino.alexandria.Timetag;
import io.intino.datahub.master.MasterDatamart;
import io.intino.datahub.master.MasterDatamart.Snapshot;

import java.io.*;
import java.util.Arrays;
import java.util.Optional;

public class SnapshotSerializer {

	public static void save(File datamartsRoot, Timetag timetag, MasterDatamart<?> datamart) throws IOException {
		File file = new File(datamartsRoot, datamart.name() + "/" + timetag.value() + ".datamart.snapshot");
		file.getParentFile().mkdirs();
		try(BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			MasterDatamartSerializer.serialize(datamart, writer);
		}
	}

	public static <T> Optional<Snapshot<T>> load(File datamartsRoot, String datamartName) throws IOException {
		File file = getMostRecentSnapshotFileFrom(new File(datamartsRoot, datamartName));
		if(file == null) return Optional.empty();
		return Optional.of(deserialize(file));
	}

	private static <T> Snapshot<T> deserialize(File file) throws IOException {
		MasterDatamart<T> datamart = MasterDatamartSerializer.deserialize(new BufferedReader(new FileReader(file)));
		return new Snapshot<>(timetagOf(file), datamart);
	}

	private static Timetag timetagOf(File file) {
		String name = file.getName();
		return Timetag.of(name.substring(name.indexOf('.')));
	}

	private static File getMostRecentSnapshotFileFrom(File file) {
		File[] files = file.listFiles(f -> f.getName().endsWith(".datamart.snapshot"));
		if(files == null || files.length == 0) return null;
		Arrays.sort(files);
		return files[files.length - 1];
	}
}
