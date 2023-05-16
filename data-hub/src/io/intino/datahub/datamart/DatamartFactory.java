package io.intino.datahub.datamart;

import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.alexandria.datalake.Datalake;
import io.intino.alexandria.event.Event;
import io.intino.alexandria.event.EventStream;
import io.intino.alexandria.event.message.MessageEvent;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.datamart.impl.LocalMasterDatamart;
import io.intino.datahub.datamart.mounters.EntityMounter;
import io.intino.datahub.datamart.mounters.ReelMounter;
import io.intino.datahub.datamart.mounters.TimelineMounter;
import io.intino.datahub.model.*;
import io.intino.datahub.model.rules.DayOfWeek;
import io.intino.datahub.model.rules.SnapshotScale;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.intino.datahub.datamart.MasterDatamart.Snapshot.shouldCreateSnapshot;

public class DatamartFactory {
	private final DataHubBox box;
	private final Datalake datalake;
	private boolean useSnapshots = true;

	public DatamartFactory(DataHubBox box, Datalake datalake) {
		this.box = box;
		this.datalake = datalake;
	}

	public DatamartFactory useSnapshots(boolean useSnapshots) {
		this.useSnapshots = useSnapshots;
		return this;
	}

	public MasterDatamart create(Datamart definition) throws IOException {
		Reference<MasterDatamart> datamart = new Reference<>();
		Reference<Timetag> fromTimetag = new Reference<>();

		if (failedToLoadLastSnapshotOf(definition, datamart, fromTimetag)) {
			datamart.value = new LocalMasterDatamart(box, definition);
			fromTimetag.value = null;
		}
		// If loaded from snapshot, reflow events between the snapshot's timetag and the most recent timetag
		// If no snapshot was loaded, reflow all events
		return reflow(datamart.value, fromTimetag.value, definition);
	}

	private boolean failedToLoadLastSnapshotOf(Datamart definition, Reference<MasterDatamart> datamart, Reference<Timetag> fromTimetag) {
		if(!useSnapshots) return true;
		Optional<MasterDatamart.Snapshot> snapshot = box.datamartSerializer().loadMostRecentSnapshot(definition.name$());
		if (snapshot.isPresent()) {
			datamart.value = snapshot.get().datamart();
			fromTimetag.value = snapshot.get().timetag();
			return false;
		}
		return true;
	}

	public MasterDatamart reflow(MasterDatamart datamart, Timetag fromTimetag, Datamart definition) throws IOException {
		SnapshotScale scale = definition.snapshots() == null ? SnapshotScale.None : Optional.ofNullable(definition.snapshots().scale()).orElse(SnapshotScale.None);
		DayOfWeek firstDayOfWeek = definition.snapshots() == null ? DayOfWeek.MONDAY : definition.snapshots().firstDayOfWeek();

		Set<String> entityTanks = entityTanks(definition);
		Set<String> timelineTanks = timelineTanks(definition);
		Set<String> reelTanks = reelTanks(definition);

		EntityMounter entityMounter = new EntityMounter(datamart);
		TimelineMounter timelineMounter = new TimelineMounter(datamart);
		ReelMounter reelMounter = new ReelMounter(datamart);

		Iterator<Event> iterator = reflowTanksFrom(fromTimetag, entityTanks, timelineTanks, reelTanks);

		reflow(
				datamart,
				scale, firstDayOfWeek,
				eventsOf(entityTanks), eventsOf(timelineTanks), eventsOf(reelTanks),
				entityMounter, timelineMounter, reelMounter,
				iterator);

		return datamart;
	}

	private void reflow(MasterDatamart datamart, SnapshotScale scale, DayOfWeek firstDayOfWeek, Set<String> entityTanks, Set<String> timelineTanks, Set<String> reelTanks, EntityMounter entityMounter, TimelineMounter timelineMounter, ReelMounter reelMounter, Iterator<Event> iterator) throws IOException {
		while (iterator.hasNext()) {
			Event event = iterator.next();

			createSnapshotIfNecessary(datamart, scale, firstDayOfWeek, event);

			if(entityTanks.contains(event.type()))
				entityMounter.mount(event);

			if(timelineTanks.contains(event.type()))
				timelineMounter.mount(event);

			if(reelTanks.contains(event.type()))
				reelMounter.mount(event);
		}
	}

	private Set<String> eventsOf(Set<String> tankNames) {
		return tankNames.stream().map(name -> name.substring(name.lastIndexOf('.') + 1)).collect(Collectors.toSet());
	}

	private void createSnapshotIfNecessary(MasterDatamart datamart, SnapshotScale scale, DayOfWeek firstDayOfWeek, Event event) throws IOException {
		if(scale == SnapshotScale.None) return;
		Timetag timetag = Timetag.of(event.ts(), Scale.Day);
		if (shouldCreateSnapshot(timetag, scale, firstDayOfWeek))
			box.datamartSerializer().saveSnapshot(timetag, datamart);
	}

	@SuppressWarnings("unchecked")
	private Iterator<Event> reflowTanksFrom(Timetag fromTimetag, Set<String> entityTanks, Set<String> timelineTanks, Set<String> reelTanks) {
		Set<String> tankNames = new HashSet<>(entityTanks);
		tankNames.addAll(timelineTanks);
		tankNames.addAll(reelTanks);
		return EventStream.merge(tanks(tankNames)
				.map(tank -> (Stream<Event>) (fromTimetag == null ? tank.content() : tankContentFrom(fromTimetag, tank)))).iterator();
	}

	private static Stream<? extends Event> tankContentFrom(Timetag fromTimetag, Datalake.Store.Tank<? extends Event> tank) {
		// TODO: OR check if this needs to be changed for timeline/reel dependent tanks
		return tank.content((ss, ts) -> !ts.isBefore(fromTimetag));
	}

	private Stream<Datalake.Store.Tank<? extends Event>> tanks(Set<String> tankNames) {
		return Stream.of(
				datalake.messageStore().tanks().filter(t -> tankNames.contains(t.name())),
				datalake.measurementStore().tanks().filter(t -> tankNames.contains(t.name())),
				datalake.resourceStore().tanks().filter(t -> tankNames.contains(t.name()))
		).flatMap(Function.identity());
	}

	private static Set<String> reelTanks(Datamart definition) {
		return definition.reelList().stream().flatMap(r -> Stream.concat(
						r.signalList().stream().map(DatamartFactory::tankName),
						r.entity().from() == null ? Stream.empty() : Stream.of(tankName(r.entity()))))
				.collect(Collectors.toSet());
	}

	private static Set<String> timelineTanks(Datamart definition) {
		return definition.timelineList().stream().flatMap(t -> Stream.of(tankName(t.tank().sensor()), tankName(t.entity()))).filter(Objects::nonNull).collect(Collectors.toSet());
	}

	private static Set<String> entityTanks(Datamart definition) {
		return definition.entityList().stream().filter(e -> e.from() != null).map(DatamartFactory::tankName).collect(Collectors.toSet());
	}

	private static String tankName(Reel.Signal s) {
		return s.tank().message().core$().fullName().replace("$", ".");
	}

	private static String tankName(Sensor sensor) {
		return sensor.core$().fullName().replace("$", ".");
	}

	private static String tankName(Entity e) {
		return e.from() == null ? null : e.from().message().core$().fullName().replace("$", ".");
	}

	private static class Reference<T> {
		public T value;
	}
}