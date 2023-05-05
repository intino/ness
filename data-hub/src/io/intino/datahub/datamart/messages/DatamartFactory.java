package io.intino.datahub.datamart.messages;

import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.alexandria.datalake.Datalake;
import io.intino.alexandria.event.EventStream;
import io.intino.alexandria.event.measurement.MeasurementEvent;
import io.intino.alexandria.event.message.MessageEvent;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.datamart.MasterDatamart;
import io.intino.datahub.datamart.impl.LocalMasterDatamart;
import io.intino.datahub.datamart.mounters.EntityMounter;
import io.intino.datahub.datamart.mounters.MasterDatamartMounter;
import io.intino.datahub.datamart.mounters.ReelMounter;
import io.intino.datahub.datamart.mounters.TimelineMounter;
import io.intino.datahub.model.Datamart;
import io.intino.datahub.model.Reel;
import io.intino.datahub.model.rules.DayOfWeek;
import io.intino.datahub.model.rules.SnapshotScale;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

import static io.intino.datahub.datamart.MasterDatamart.Snapshot.shouldCreateSnapshot;

public class DatamartFactory {
	private final DataHubBox box;
	private final Datalake datalake;

	public DatamartFactory(DataHubBox box, Datalake datalake) {
		this.box = box;
		this.datalake = datalake;
	}

	public MasterDatamart create(Datamart definition) throws IOException {
		Reference<MasterDatamart> datamart = new Reference<>();
		Reference<Timetag> fromTimetag = new Reference<>();

		if (failedToLoadLastBackupOf(definition, datamart, fromTimetag)) {
			if (failedToLoadLastSnapshotOf(definition, datamart, fromTimetag)) {
				datamart.value = new LocalMasterDatamart(box, definition);
				fromTimetag.value = null;
			}
			// If loaded from snapshot, reflow events between the snapshot's timetag and the most recent timetag
			// If no snapshot was loaded, reflow all events
			reflow(datamart.value, fromTimetag.value, definition);
		} // If loaded from backup, no reflow is performed
		return datamart.value;
	}

	private boolean failedToLoadLastBackupOf(Datamart definition, Reference<MasterDatamart> datamart, Reference<Timetag> fromTimetag) {
		File backup = box.datamartSerializer().backupFileOf(definition.name$());
		if (!backup.exists()) return true;
		try {
			datamart.value = box.datamartSerializer().deserialize(backup, definition);
		} catch (IOException e) {
			Logger.error("Could not deserialize datamart " + definition.name$() + " from " + backup + ": " + e.getMessage(), e);
			return true;
		}
		return false;
	}

	private boolean failedToLoadLastSnapshotOf(Datamart definition, Reference<MasterDatamart> datamart, Reference<Timetag> fromTimetag) {
		Optional<MasterDatamart.Snapshot> snapshot = box.datamartSerializer().loadMostRecentSnapshot(definition.name$());
		if (snapshot.isPresent()) {
			datamart.value = snapshot.get().datamart();
			fromTimetag.value = snapshot.get().timetag();
			return false;
		}
		return true;
	}

	private void reflow(MasterDatamart datamart, Timetag fromTimetag, Datamart definition) throws IOException {
		reflowEntityEvents(datamart, fromTimetag, definition);
		if (box.datamartTimelineFiles(datamart.name()).isEmpty())
			reflowTimelineEvents(datamart, null, definition);
		if (box.datamartReelFiles(datamart.name()).isEmpty())
			reflowReelEvents(datamart, definition);
		Logger.info("Reflow finished for datamart " + datamart.name());
	}

	private void reflowReelEvents(MasterDatamart datamart, Datamart definition) {
		MasterDatamartMounter mounter = new ReelMounter(datamart);
		definition.reelList().stream()
				.map(this::eventsForReel)
				.forEach(events -> events.forEach(e -> mounter.mount(e.toMessage())));
	}

	private void reflowTimelineEvents(MasterDatamart datamart, Timetag fromTimetag, Datamart definition) {
		TimelineMounter mounter = new TimelineMounter(datamart);
		Iterator<MeasurementEvent> iterator = reflowTimelineTanksFrom(fromTimetag, definition);
		while (iterator.hasNext()) mounter.mount(iterator.next());
	}

	private void reflowEntityEvents(MasterDatamart datamart, Timetag fromTimetag, Datamart definition) throws IOException {
		MasterDatamartMounter mounter = new EntityMounter(datamart);
		SnapshotScale scale = definition.snapshots() == null ? SnapshotScale.None : Optional.ofNullable(definition.snapshots().scale()).orElse(SnapshotScale.None);
		DayOfWeek firstDayOfWeek = definition.snapshots() == null ? DayOfWeek.MONDAY : definition.snapshots().firstDayOfWeek();
		Iterator<MessageEvent> iterator = reflowEntityTanksFrom(fromTimetag, definition);
		while (iterator.hasNext()) {
			MessageEvent event = iterator.next();
			Timetag timetag = Timetag.of(event.ts(), Scale.Day);
			if (shouldCreateSnapshot(timetag, scale, firstDayOfWeek))
				box.datamartSerializer().saveSnapshot(timetag, datamart);
			mounter.mount(event.toMessage());
		}
	}

	private Stream<MessageEvent> eventsForReel(Reel reel) {
		if (reel.signalList().isEmpty()) return Stream.empty();
		return EventStream.merge(reel.signalList().stream()
				.map(this::tankOf)
				.map(Datalake.Store.Tank::content));
	}

	private Datalake.Store.Tank<MessageEvent> tankOf(Reel.Signal s) {
		return datalake.messageStore().tank(s.tank().message().core$().fullName().replace("$", "."));
	}

	private Iterator<MeasurementEvent> reflowTimelineTanksFrom(Timetag fromTimetag, Datamart definition) {
		return definition.timelineList().stream()
				.filter(e -> e.tank().sensor() != null)
				.map(e -> datalake.measurementStore().tank(e.tank().sensor().core$().fullName().replace("$", ".")))
				.flatMap(t -> fromTimetag == null ? t.content() : t.content((ss, ts) -> !ts.isBefore(fromTimetag)))
				.iterator();
	}

	// Events are NOT merged across tanks
	private Iterator<MessageEvent> reflowEntityTanksFrom(Timetag fromTimetag, Datamart definition) {
		return definition.entityList().stream()
				.filter(e -> e.from() != null)
				.map(e -> datalake.messageStore().tank(e.from().message().core$().fullName().replace("$", ".")))
				.flatMap(t -> fromTimetag == null ? t.content() : t.content((ss, ts) -> !ts.isBefore(fromTimetag)))
				.iterator();
	}

	private static class Reference<T> {
		public T value;
	}
}