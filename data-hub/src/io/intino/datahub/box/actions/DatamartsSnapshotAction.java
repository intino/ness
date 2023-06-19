package io.intino.datahub.box.actions;

import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.datamart.MasterDatamart;
import io.intino.datahub.model.Datamart;
import io.intino.datahub.model.rules.DayOfWeek;
import io.intino.datahub.model.rules.SnapshotScale;

import java.io.File;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.NoSuchElementException;

import static io.intino.datahub.datamart.MasterDatamart.Snapshot.shouldCreateSnapshot;

public class DatamartsSnapshotAction {

	public DataHubBox box;

	public DatamartsSnapshotAction() {}

	public DatamartsSnapshotAction(DataHubBox box) {
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
			File snapshotsDir = box.datamartSerializer().snapshotDirOf(datamart.name());
			File[] files = snapshotsDir.listFiles(f -> f.getName().endsWith(".zim") && Timetag.isTimetag(f.getName().replace(".zim", "")));
			int maxCount = definitionOf(datamart).snapshots().maxCount();
			if(files == null || maxCount < 0) continue;
			if(files.length > maxCount) {
				Arrays.stream(files).sorted().limit(maxCount - files.length).forEach(File::delete);
			}
		}
	}

	private void createSnapshotIfNecessary(Timetag today, MasterDatamart datamart) {
		synchronized (datamart) {
			try {
				Datamart definition = definitionOf(datamart);
				if(definition.snapshots() == null) return;
				SnapshotScale scale = definition.snapshots().scale();
				if(scale == null) return;
				DayOfWeek firstDayOfWeek = definition.snapshots().firstDayOfWeek();
				if(firstDayOfWeek == null) firstDayOfWeek = DayOfWeek.MONDAY;

				if(shouldCreateSnapshot(today, scale, firstDayOfWeek)) {
					box.datamartSerializer().saveSnapshot(today, datamart);
				}
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
