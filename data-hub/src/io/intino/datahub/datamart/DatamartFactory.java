package io.intino.datahub.datamart;

import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.alexandria.datalake.Datalake;
import io.intino.alexandria.event.Event;
import io.intino.alexandria.event.EventStream;
import io.intino.alexandria.event.measurement.MeasurementEvent;
import io.intino.alexandria.event.message.MessageEvent;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.datamart.impl.LocalMasterDatamart;
import io.intino.datahub.datamart.mounters.*;
import io.intino.datahub.model.Datamart;
import io.intino.datahub.model.Entity;
import io.intino.datahub.model.Sensor;
import io.intino.datahub.model.Timeline;
import io.intino.datahub.model.rules.SnapshotScale;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DatamartFactory {
	private final DataHubBox box;
	private final Datalake datalake;

	public DatamartFactory(DataHubBox box, Datalake datalake) {
		this.box = box;
		this.datalake = datalake;
	}

	public MasterDatamart create(Datamart definition) throws Exception {
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

	public MasterDatamart reflow(MasterDatamart datamart, Instant fromTs, Datamart definition) throws Exception {
		SnapshotScale scale = definition.snapshots() == null ? SnapshotScale.None : Optional.ofNullable(definition.snapshots().scale()).orElse(SnapshotScale.None);

		Set<String> entityTanks = entityTanks(definition);
		Set<String> cookedTimelineTanks = cookedTimelinesTanks(definition);
		Set<String> reelTanks = reelTanks(definition);

		Logger.info("Reflowing entities and cooked timelines...");
		reflow(new EntityMounter(datamart), new TimelineMounter(datamart), entityTanks, cookedTimelineTanks, reflowTanks(fromTs, entityTanks, cookedTimelineTanks));

		Logger.info("Reflowing raw timelines...");
		reflowTimelines(datamart, definition, fromTs, scale);

		Logger.info("Reflowing reels...");
		reflow(new ReelMounter(datamart), reflowTanks(fromTs, reelTanks));

		Logger.info("Reflow complete");

		box.datamartSerializer().saveSnapshot(Timetag.today(), datamart);

		return datamart;
	}

	private void reflow(MasterDatamartMounter mounters, Iterator<Event> events) {
		while(events.hasNext()) {
			mounters.mount(events.next());
		}
	}

	private void reflow(EntityMounter entityMounter, TimelineMounter timelineMounter, Set<String> entityTanks, Set<String> cookedTimelineTanks, Iterator<Event> events) {
		while(events.hasNext()) {
			Event event = events.next();
			if(entityTanks.contains(event.type())) entityMounter.mount(event);
			if(cookedTimelineTanks.contains(event.type())) timelineMounter.mount(event);
		}
	}

	private Set<String> cookedTimelinesTanks(Datamart definition) {
		return definition.timelineList().stream()
				.filter(Timeline::isCooked)
				.map(TimelineUtils::getCookedTanks)
				.flatMap(Collection::stream)
				.collect(Collectors.toSet());
	}

	// Reflow each ss independently to avoid too many files open error
	private void reflowTimelines(MasterDatamart datamart, Datamart definition, Instant fromTs, SnapshotScale scale) throws InterruptedException {
		ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()-1);
		for(var rawTimeline : definition.timelineList(t -> t.isRaw() && tankName(t.entity()) != null)) {
			String entityTankName = tankName(rawTimeline.entity());
			Datalake.Store.Tank<MessageEvent> messageTank = datalake.messageStore().tank(entityTankName);
			Datalake.Store.Tank<MeasurementEvent> measurementTank = datalake.measurementStore().tank(tankName(rawTimeline.asRaw().tank().sensor()));
			threadPool.execute(() -> measurementTank.sources().forEach(ss -> reflowTimelinesOf(
					datamart,
					getSimpleName(measurementTank.name()),
					ss,
					messageTank,
					fromTs,
					scale)
			));
		}
		threadPool.shutdown();
		threadPool.awaitTermination(1, TimeUnit.DAYS);// TODO:check
	}

	private String getSimpleName(String name) {
		return name.substring(name.lastIndexOf('.') + 1);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private void reflowTimelinesOf(MasterDatamart datamart, String measurementTank, Datalake.Store.Source<MeasurementEvent> ss, Datalake.Store.Tank<MessageEvent> messageTank, Instant fromTs, SnapshotScale scale) {
		try(TimelineMounter.OfSingleTimeline mounter = new TimelineMounter.OfSingleTimeline(datamart, measurementTank, ss.name())) {
			Stream content = messageTank.content();
			Stream measurements = EventStream.sequence(ss.tubs().map(Datalake.Store.Tub::eventSupplier).toList());
			Iterator<Event> events = EventStream.merge(Stream.of(content, measurements)).iterator();
			while(events.hasNext()) {
				mounter.mount(events.next());
			}
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	private Set<String> eventsOf(Set<String> tankNames) {
		return tankNames.stream().map(name -> name.substring(name.lastIndexOf('.') + 1)).collect(Collectors.toSet());
	}

	@SuppressWarnings("unchecked")
	private Iterator<Event> reflowTanks(Instant fromTs, Set<String>... tanks) {
		Set<String> tankNames = new HashSet<>();
		Arrays.stream(tanks).forEach(tankNames::addAll);
		if (fromTs != null) {
			Timetag fromTimetag = Timetag.of(fromTs, Scale.Minute);
			return EventStream.merge(tanks(tankNames).map(tank -> (Stream<Event>) tank.content((ss, tt) -> tt.isAfter(fromTimetag))))
					.filter(e -> e.ts().isAfter(fromTs))
					.iterator();
		}

		return EventStream.merge(tanks(tankNames).map(tank -> (Stream<Event>) tank.content())).iterator();
	}

	@SuppressWarnings("unchecked")
	private Iterator<Event> reflowTanks(Set<String> entityTanks, Set<String> timelineTanks, Set<String> reelTanks, Instant fromTs) {
		Set<String> tankNames = new HashSet<>(entityTanks);
		tankNames.addAll(timelineTanks);
		tankNames.addAll(reelTanks);

		if (fromTs != null) {
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
		return definition.timelineList().stream()
				.flatMap(TimelineUtils::tanksOf)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
	}

	public static Stream<String> tanksOf(Timeline.Cooked.TimeSeries.Count ts) {
		return ts.operationList().stream().map(d -> tankName(d.tank()));
	}

	private static Set<String> entityTanks(Datamart definition) {
		return definition.entityList().stream().filter(e -> e.from() != null).map(DatamartFactory::tankName).collect(Collectors.toSet());
	}

	private static String tankName(io.intino.datahub.model.Datalake.Tank.Message tank) {
		return tank.message().core$().fullName().replace("$", ".");
	}

	private static String tankName(Entity e) {
		return e.from() == null ? null : e.from().message().core$().fullName().replace("$", ".");
	}

	private static String tankName(Sensor sensor) {
		return sensor.core$().fullName().replace("$", ".");
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