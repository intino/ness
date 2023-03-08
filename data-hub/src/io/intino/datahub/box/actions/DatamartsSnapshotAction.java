package io.intino.datahub.box.actions;

import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.datamart.MasterDatamart;
import io.intino.datahub.datamart.serialization.MasterDatamartSnapshots;
import io.intino.datahub.model.rules.SnapshotScale;

import java.time.LocalDate;

import static io.intino.datahub.datamart.MasterDatamart.Snapshot.shouldCreateSnapshot;

public class DatamartsSnapshotAction {

	public DataHubBox box;

	public DatamartsSnapshotAction() {}

	public DatamartsSnapshotAction(DataHubBox box) {
		this.box = box;
	}

	public void execute() {
		synchronized (DatamartsSnapshotAction.class) {
			Timetag today = Timetag.of(LocalDate.now(), Scale.Day);
			box.datamarts().datamarts().parallelStream().forEach(datamart -> createSnapshotIfNecessary(today, datamart));
		}
	}

	private void createSnapshotIfNecessary(Timetag today, MasterDatamart<?> datamart) {
		try {
			if(shouldCreateSnapshot(today, scaleOf(datamart)))
				MasterDatamartSnapshots.saveSnapshot(box.datamarts().root(), today, datamart);
		} catch (Throwable e) {
			Logger.error("Failed to handle snapshot of " + datamart.name() + ": " + e.getMessage(), e);
		}
	}

	private SnapshotScale scaleOf(MasterDatamart<?> datamart) {
		return box.graph().datamartList(d -> d.name$().equals(datamart.name())).findFirst().get().scale();
	}
}
