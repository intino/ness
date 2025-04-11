package systems.intino.eventsourcing.datahub.box.actions;

import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.alexandria.logger.Logger;
import systems.intino.eventsourcing.datahub.box.DatahubBox;
import systems.intino.eventsourcing.datahub.datamart.MasterDatamart;
import systems.intino.eventsourcing.datahub.datamart.snapshots.DatamartSnapshotManager;
import systems.intino.eventsourcing.datahub.model.Datamart;
import systems.intino.eventsourcing.datahub.model.rules.DayOfWeek;
import systems.intino.eventsourcing.datahub.model.rules.SnapshotScale;

import java.io.File;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.NoSuchElementException;

import static systems.intino.eventsourcing.datahub.datamart.MasterDatamart.Snapshot.shouldCreateSnapshot;

public class DatamartsSnapshotAction {
	public DatahubBox box;

	public DatamartsSnapshotAction(DatahubBox box) {
		this.box = box;
	}

	public void execute() {
		synchronized (DatamartsSnapshotAction.class) {
			Timetag timetag = Timetag.of(LocalDate.now(), Scale.Day).previous();
			box.datamarts().datamarts().parallelStream().forEach(datamart -> createSnapshotIfNecessary(timetag, datamart));
			removeOldSnapshots();
		}
	}

	private void removeOldSnapshots() {
		for (MasterDatamart datamart : box.datamarts().datamarts()) {
			File snapshotsDir = new DatamartSnapshotManager(box).snapshotDirOf(datamart.name());
			File[] files = snapshotsDir.listFiles(f -> f.getName().endsWith(".zim") && Timetag.isTimetag(f.getName().replace(".zim", "")));
			int maxCount = definitionOf(datamart).snapshots().maxCount();
			if (files == null || maxCount < 0) continue;
			if (files.length > maxCount) {
				Arrays.stream(files).sorted().limit(maxCount - files.length).forEach(File::delete);
			}
		}
	}

	private void createSnapshotIfNecessary(Timetag today, MasterDatamart datamart) {
		synchronized (datamart) {
			try {
				Datamart definition = definitionOf(datamart);
				if (definition.snapshots() == null) return;
				SnapshotScale scale = definition.snapshots().scale();
				if (scale == null) return;
				DayOfWeek firstDayOfWeek = definition.snapshots().firstDayOfWeek();
				if (firstDayOfWeek == null) firstDayOfWeek = DayOfWeek.MONDAY;
				if (shouldCreateSnapshot(today, scale, firstDayOfWeek))
					new DatamartSnapshotManager(box).saveSnapshot(today, datamart);
			} catch (Throwable e) {
				Logger.error("Failed to handle snapshot of " + datamart.name() + ": " + e.getMessage(), e);
			}
		}
	}

	private Datamart definitionOf(MasterDatamart datamart) {
		return box.graph().datamartList(d -> d.name$().equals(datamart.name())).findFirst().orElseThrow(() ->
				new NoSuchElementException("No datamart named " + datamart.name() + " defined in ness model")
		);
	}
}