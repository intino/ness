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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.intino.datahub.datamart.MasterDatamart.Snapshot.shouldCreateSnapshot;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class DatamartFactory {
	private final DataHubBox box;
	private final Datalake datalake;

	public DatamartFactory(DataHubBox box, Datalake datalake) {
		this.box = box;
		this.datalake = datalake;
	}

	public MasterDatamart create(Datamart definition) throws IOException {
		File datamartDir = box.datamartDirectory(definition.name$());
		FileUtils.deleteDirectory(datamartDir);

		try {
			return reflow(new LocalMasterDatamart(box, definition), definition);
		} catch (Exception e) {
			Logger.error("Error while performing complete reflow: " + e.getMessage() + ". Datamart directory will be rolled back.", e);
			return null;
		}
	}

	public MasterDatamart reflow(MasterDatamart datamart, Datamart definition) throws IOException {
		SnapshotScale scale = definition.snapshots() == null ? SnapshotScale.None : Optional.ofNullable(definition.snapshots().scale()).orElse(SnapshotScale.None);
		DayOfWeek firstDayOfWeek = definition.snapshots() == null ? DayOfWeek.MONDAY : definition.snapshots().firstDayOfWeek();

		Set<String> entityTanks = entityTanks(definition);
		Set<String> timelineTanks = timelineTanks(definition);
		Set<String> reelTanks = reelTanks(definition);

		EntityMounter entityMounter = new EntityMounter(datamart);
		TimelineMounter timelineMounter = new TimelineMounter(datamart);
		ReelMounter reelMounter = new ReelMounter(datamart);

		Iterator<Event> iterator = reflowTanks(entityTanks, timelineTanks, reelTanks);

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

			if (entityTanks.contains(event.type()))
				entityMounter.mount(event);

			if (timelineTanks.contains(event.type()))
				timelineMounter.mount(event);

			if (reelTanks.contains(event.type()))
				reelMounter.mount(event);
		}
	}

	private Set<String> eventsOf(Set<String> tankNames) {
		return tankNames.stream().map(name -> name.substring(name.lastIndexOf('.') + 1)).collect(Collectors.toSet());
	}

	private void createSnapshotIfNecessary(MasterDatamart datamart, SnapshotScale scale, DayOfWeek firstDayOfWeek, Event event) throws IOException {
		if (scale == SnapshotScale.None) return;
		Timetag timetag = Timetag.of(event.ts(), Scale.Day);
		if (shouldCreateSnapshot(timetag, scale, firstDayOfWeek))
			box.datamartSerializer().saveSnapshot(timetag, datamart);
	}

	@SuppressWarnings("unchecked")
	private Iterator<Event> reflowTanks(Set<String> entityTanks, Set<String> timelineTanks, Set<String> reelTanks) {
		Set<String> tankNames = new HashSet<>(entityTanks);
		tankNames.addAll(timelineTanks);
		tankNames.addAll(reelTanks);
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
}