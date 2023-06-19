package io.intino.datahub.datamart;

import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.alexandria.datalake.Datalake;
import io.intino.alexandria.event.Event;
import io.intino.alexandria.event.EventStream;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.datamart.impl.LocalMasterDatamart;
import io.intino.datahub.datamart.mounters.EntityMounter;
import io.intino.datahub.datamart.mounters.ReelMounter;
import io.intino.datahub.datamart.mounters.TimelineMounter;
import io.intino.datahub.model.Datamart;
import io.intino.datahub.model.Entity;
import io.intino.datahub.model.Sensor;
import io.intino.datahub.model.rules.DayOfWeek;
import io.intino.datahub.model.rules.SnapshotScale;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
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
		Reference<Instant> fromTs = new Reference<>();

		if (failedToLoadLastSnapshotOf(definition, datamart, fromTs)) {
			datamart.value = new LocalMasterDatamart(box, definition);
			fromTs.value = null;
		}
		// If loaded from snapshot, reflow events between the snapshot's timetag and the most recent timetag
		// If no snapshot was loaded, reflow all events
		return reflow(datamart.value, fromTs.value, definition);
	}

	private boolean failedToLoadLastSnapshotOf(Datamart definition, Reference<MasterDatamart> datamart, Reference<Instant> fromTs) {
		Optional<MasterDatamart.Snapshot> snapshot = box.datamartSerializer().loadMostRecentSnapshot(definition.name$());
		if (snapshot.isPresent()) {
			datamart.value = snapshot.get().datamart();
			fromTs.value = snapshot.get().datamart().ts();
			return false;
		}
		return true;
	}


	public MasterDatamart reflow(MasterDatamart datamart, Instant fromTs, Datamart definition) throws IOException {
		SnapshotScale scale = definition.snapshots() == null ? SnapshotScale.None : Optional.ofNullable(definition.snapshots().scale()).orElse(SnapshotScale.None);
		DayOfWeek firstDayOfWeek = definition.snapshots() == null ? DayOfWeek.MONDAY : definition.snapshots().firstDayOfWeek();

		Set<String> entityTanks = entityTanks(definition);
		Set<String> timelineTanks = timelineTanks(definition);
		Set<String> reelTanks = reelTanks(definition);

		EntityMounter entityMounter = new EntityMounter(datamart);
		TimelineMounter timelineMounter = new TimelineMounter(datamart);
		ReelMounter reelMounter = new ReelMounter(datamart);

		Iterator<Event> iterator = reflowTanks(entityTanks, timelineTanks, reelTanks, fromTs);

		reflow(
				datamart,
				scale, firstDayOfWeek,
				eventsOf(entityTanks), eventsOf(timelineTanks), eventsOf(reelTanks),
				entityMounter, timelineMounter, reelMounter,
				iterator);

		return datamart;
	}

	private void reflow(MasterDatamart datamart, SnapshotScale scale, DayOfWeek firstDayOfWeek, Set<String> entityTanks, Set<String> timelineTanks, Set<String> reelTanks, EntityMounter entityMounter, TimelineMounter timelineMounter, ReelMounter reelMounter, Iterator<Event> iterator) throws IOException {
		Timetag oldTimetag = null;

		while (iterator.hasNext()) {
			Event event = iterator.next();

//			createSnapshotIfNecessary(datamart, scale, firstDayOfWeek, event, oldTimetag);

			if (entityTanks.contains(event.type()))
				entityMounter.mount(event);

			if (timelineTanks.contains(event.type()))
				timelineMounter.mount(event);

			if (reelTanks.contains(event.type()))
				reelMounter.mount(event);

			oldTimetag = Timetag.of(event.ts(), Scale.Day);
		}

		box.datamartSerializer().saveSnapshot(Timetag.today(), datamart);
	}

	private Set<String> eventsOf(Set<String> tankNames) {
		return tankNames.stream().map(name -> name.substring(name.lastIndexOf('.') + 1)).collect(Collectors.toSet());
	}

	private void createSnapshotIfNecessary(MasterDatamart datamart, SnapshotScale scale, DayOfWeek firstDayOfWeek, Event event, Timetag oldTimetag) throws IOException {
		if (scale == SnapshotScale.None || oldTimetag == null) return;
		Timetag timetag = Timetag.of(event.ts(), Scale.Day);
		if (shouldCreateSnapshot(oldTimetag, timetag, scale, firstDayOfWeek))
			box.datamartSerializer().saveSnapshot(timetag, datamart);
	}

	@SuppressWarnings("unchecked")
	private Iterator<Event> reflowTanks(Set<String> entityTanks, Set<String> timelineTanks, Set<String> reelTanks, Instant fromTs) {
		Set<String> tankNames = new HashSet<>(entityTanks);
		tankNames.addAll(timelineTanks);
		tankNames.addAll(reelTanks);

		if(fromTs != null) {
			Timetag fromTimetag = Timetag.of(fromTs, Scale.Minute);
			return EventStream.merge(tanks(tankNames).map(tank -> (Stream<Event>) tank.content((ss, tt) -> tt.isAfter(fromTimetag))))
					.filter(e -> e.ts().isAfter(fromTs))
					.iterator();
		}

		return EventStream.merge(tanks(tankNames).map(tank -> (Stream<Event>) tank.content())).iterator();
	}

	private Stream<Datalake.Store.Tank<? extends Event>> tanks(Set<String> tankNames) {
		return Stream.of(
				datalake.messageStore().tanks().filter(t -> tankNames.contains(t.name())),
				datalake.measurementStore().tanks().filter(t -> tankNames.contains(t.name())),
				datalake.resourceStore().tanks().filter(t -> tankNames.contains(t.name()))
		).flatMap(Function.identity());
	}

	private static Set<String> reelTanks(Datamart definition) {
		return definition.reelList().stream().map(r -> tankName(r.tank()))
				.collect(Collectors.toSet());
	}

	private static Set<String> timelineTanks(Datamart definition) {
		return definition.timelineList().stream().flatMap(t -> Stream.of(tankName(t.tank().sensor()), tankName(t.entity()))).filter(Objects::nonNull).collect(Collectors.toSet());
	}

	private static Set<String> entityTanks(Datamart definition) {
		return definition.entityList().stream().filter(e -> e.from() != null).map(DatamartFactory::tankName).collect(Collectors.toSet());
	}

	private static String tankName(io.intino.datahub.model.Datalake.Tank.Message tank) {
		return tank.message().core$().fullName().replace("$", ".");
	}

	private static String tankName(Sensor sensor) {
		return sensor.core$().fullName().replace("$", ".");
	}

	private static String tankName(Entity e) {
		return e.from() == null ? null : e.from().message().core$().fullName().replace("$", ".");
	}

	private void deleteDirectorySafe(File backup) {
		try {
			FileUtils.deleteDirectory(backup);
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	private static class Reference<T> {
		private T value;
	}
}