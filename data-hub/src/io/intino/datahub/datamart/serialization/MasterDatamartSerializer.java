package io.intino.datahub.datamart.serialization;

import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.Message;
import io.intino.alexandria.zim.ZimStream;
import io.intino.alexandria.zim.ZimWriter;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.datamart.MasterDatamart;
import io.intino.datahub.datamart.impl.LocalMasterDatamart;
import io.intino.datahub.model.Datamart;

import java.io.*;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.reverseOrder;

public class MasterDatamartSerializer {

	public static final String SNAPSHOT_EXTENSION = ".dm-snapshot.zim";

	private final DataHubBox box;

	public MasterDatamartSerializer(DataHubBox box) {
		this.box = box;
	}

	public void serialize(MasterDatamart datamart, File file) throws IOException {
		file.getParentFile().mkdirs();
		serialize(datamart, new FileOutputStream(file));
	}

	public void serialize(MasterDatamart datamart, OutputStream outputStream) throws IOException {
		try (ZimWriter writer = new ZimWriter(outputStream)) {
			Iterator<Message> messages = datamart.entityStore().stream().iterator();
			while (messages.hasNext()) {
				writer.write(messages.next());
			}
		}
	}

	public MasterDatamart deserialize(File file, Datamart definition) throws IOException {
		return deserialize(new FileInputStream(file), definition);
	}

	public MasterDatamart deserialize(InputStream inputStream, Datamart definition) throws IOException {
		try (Stream<Message> messages = ZimStream.of(inputStream)) {
			return new LocalMasterDatamart(box, definition).reflow(messages);
		}
	}

	public void saveBackup(MasterDatamart datamart) throws IOException {
		serialize(datamart, backupFileOf(datamart.name()));
	}

	public File backupFileOf(String datamart) {
		return new File(box.datamartDirectory(datamart), ".backup");
	}

	public void saveSnapshot(Timetag timetag, MasterDatamart datamart) throws IOException {
		File file = snapshotDirOf(datamart.name() + "/" + timetag.value() + SNAPSHOT_EXTENSION);
		file.getParentFile().mkdirs();
		serialize(datamart, new FileOutputStream(file));
	}

	public List<Timetag> listAvailableSnapshotsOf(String datamartName) {
		return listSnapshotFilesIn(snapshotDirOf(datamartName)).stream()
				.map(this::timetagOf)
				.collect(Collectors.toList());
	}

	public Optional<MasterDatamart.Snapshot> loadMostRecentSnapshot(String datamartName) {
		return loadMostRecentSnapshotTo(datamartName, Timetag.of(LocalDate.now(), Scale.Day));
	}

	public Optional<MasterDatamart.Snapshot> loadMostRecentSnapshotTo(String datamartName, Timetag timetag) {
		return findSnapshotFileOf(snapshotDirOf(datamartName), timetag).map(f -> loadSnapshot(f, datamartName));
	}

	private MasterDatamart.Snapshot loadSnapshot(File file, String datamartName) {
		try {
			MasterDatamart datamart = deserialize(new FileInputStream(file), definitionOf(datamartName));
			return new MasterDatamart.Snapshot(timetagOf(file), datamart);
		} catch (IOException e) {
			Logger.error("Failed to deserialize datamart snapshot " + file.getName() + ": " + e.getMessage(), e);
			return null;
		}
	}

	private Datamart definitionOf(String name) {
		return box.graph().datamartList().stream()
				.filter(d -> d.name$().equals(name))
				.findFirst().orElseThrow(() -> new IllegalArgumentException("No datamart named " + name + " defined"));
	}

	private File snapshotDirOf(String datamartName) {
		return new File(box.datamartsRoot(), datamartName);
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
