package io.intino.datahub.datamart;

import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.alexandria.datalake.Datalake;
import io.intino.alexandria.event.Event;
import io.intino.alexandria.event.EventStream;
import io.intino.alexandria.event.measurement.MeasurementEvent;
import io.intino.alexandria.event.message.MessageEvent;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.zim.ZimStream;
import io.intino.alexandria.zim.ZimWriter;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.datamart.impl.LocalMasterDatamart;
import io.intino.datahub.datamart.mounters.EntityMounter;
import io.intino.datahub.datamart.mounters.MounterUtils;
import io.intino.datahub.datamart.mounters.ReelMounter;
import io.intino.datahub.datamart.mounters.timelines.IndicatorMounter;
import io.intino.datahub.datamart.mounters.timelines.TimelineCookedMounter;
import io.intino.datahub.datamart.mounters.timelines.TimelineMounter.OfSingleTimeline;
import io.intino.datahub.model.Datamart;
import io.intino.datahub.model.Entity;
import io.intino.datahub.model.Sensor;
import io.intino.datahub.model.Timeline;
import io.intino.sumus.chronos.TimelineStore;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@SuppressWarnings("unchecked")
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
		return reflow(datamart.value, definition);
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

	public MasterDatamart reflow(MasterDatamart datamart, Datamart definition) throws Exception {
		removeAllChronosFiles(datamart);
		reflowEntities(datamart, entityTanks(definition));
		reflowCookedTimelines(datamart, cookedTimelinesTanks(definition));
		reflowRawTimelines(datamart, definition);
		reflowIndicators(datamart, definition);
		reflowReels(datamart, reelTanks(definition));
		Logger.debug("Reflow complete");
		box.datamartSerializer().saveSnapshot(Timetag.today(), datamart);
		return datamart;
	}

	private void removeAllChronosFiles(MasterDatamart datamart) throws IOException {
		FileUtils.deleteDirectory(box.datamartTimelinesDirectory(datamart.name()));
		FileUtils.deleteDirectory(box.datamartReelsDirectory(datamart.name()));
		FileUtils.deleteDirectory(box.datamartIndicatorsDirectory(datamart.name()));
	}

	private void reflowEntities(MasterDatamart datamart, Set<String> entityTanks) {
		Logger.debug("Reflowing entities...");
		reflow(new EntityMounter(datamart), reflowTanks(entityTanks));
	}

	private void reflowRawTimelines(MasterDatamart datamart, Datamart definition) {
		Logger.debug("Reflowing raw timelines...");
		reflowTimelines(datamart, definition);
	}

	private void reflowCookedTimelines(MasterDatamart datamart, Set<String> cookedTimelineTanks) {
		Logger.debug("Reflowing cooked timelines...");
		reflow(new TimelineCookedMounter.OneShot(box, datamart, MounterUtils.timelineTypes(datamart)), reflowTanks(cookedTimelineTanks));
	}

	private void reflowIndicators(MasterDatamart datamart, Datamart definition) {
		Logger.debug("Reflowing indicators...");
		IndicatorMounter indicatorMounter = new IndicatorMounter(datamart);
		Map<String, Timeline> definitions = definition.timelineList().stream()
				.filter(Timeline::isIndicator)
				.collect(Collectors.toMap(t -> t.isRaw() ? t.asRaw().tank().asTank().asMeasurement().sensor().name$() : t.asCooked().name$(), t -> t));
		datamart.timelineStore().listFiles().stream()
				.filter(f -> definitions.containsKey(f.getParentFile().getName()))
				.forEach(f -> indicatorMounter.mount(f.getParentFile().getName(), timelineStore(f)));
	}

	private void reflowReels(MasterDatamart datamart, Set<String> tanks) {
		Logger.debug("Reflowing reels...");
		for (String tankName : tanks) {
			Datalake.Store.Tank<MessageEvent> tank = datalake.messageStore().tank(tankName);
			try (ReelMounter.Reflow mounter = new ReelMounter.Reflow(datamart)) {
				tank.content().forEach(mounter::mount);
			}
		}
	}

	private void reflow(TimelineCookedMounter timelineMounter, Iterator<Event> events) {
		while (events.hasNext()) timelineMounter.mount((MessageEvent) events.next());
	}

	private static TimelineStore timelineStore(File f) {
		try {
			return TimelineStore.of(f);
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	private void reflow(EntityMounter entityMounter, Iterator<Event> events) {
		while (events.hasNext()) {
			entityMounter.mount(events.next());
		}
	}

	private Set<String> cookedTimelinesTanks(Datamart definition) {
		return definition.timelineList().stream()
				.filter(Timeline::isCooked)
				.map(MounterUtils::getCookedTanks)
				.flatMap(Collection::stream)
				.collect(Collectors.toSet());
	}

	// Reflow each ss independently to avoid too many files open error
	private void reflowTimelines(MasterDatamart datamart, Datamart definition) {
		for (Timeline timeline : definition.timelineList(Timeline::isRaw)) {
			Supplier<MessageEventStream> messageEvents = bake(messageTanksOf(definition, timeline));
			Datalake.Store.Tank<MeasurementEvent> measurementTank = datalake.measurementStore().tank(tankName(timeline.asRaw().tank().sensor()));
			measurementTank.sources().forEach(ss ->
					reflowTimelinesOf(
							datamart,
							timeline,
							getTankEventName(measurementTank.name()),
							ss,
							messageEvents.get())
			);
		}
	}

	private Supplier<MessageEventStream> bake(List<Datalake.Store.Tank<MessageEvent>> tanks) {
		File file = bakeEventsInCacheFile(tanks);
		if (file.length() < Runtime.getRuntime().freeMemory() * 0.8) {
			return () -> {
				var events = new MessageEventStream.InMemory(file);
				file.delete();
				return events;
			};
		}
		return () -> new MessageEventStream.Reading(file);
	}

	private File bakeEventsInCacheFile(List<Datalake.Store.Tank<MessageEvent>> tanks) {
		File file = new File(box.datamartsDirectory(), ".tmp" + File.separator + System.nanoTime() + ".tmp");
		file.getParentFile().mkdirs();
		file.deleteOnExit();
		try (ZimWriter writer = new ZimWriter(file)) {
			EventStream.merge(tanks.stream().map(Datalake.Store.Tank::content)).forEach(e -> {
				try {
					writer.write(e.toMessage());
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return file;
	}

	private List<Datalake.Store.Tank<MessageEvent>> messageTanksOf(Datamart definition, Timeline timeline) {
		Entity entity = timeline.entity();
		List<Datalake.Store.Tank<MessageEvent>> tanks = new ArrayList<>();
		if (tankName(entity) != null) tanks.add(datalake.messageStore().tank(tankName(entity)));
		definition.entityList(e -> isDescendantOf(e, entity)).stream()
				.map(DatamartFactory::tankName)
				.map(tank -> datalake.messageStore().tank(tank))
				.forEach(tanks::add);
		return tanks;
	}

	private static boolean isDescendantOf(Entity node, Entity expectedParent) {
		if (!node.isExtensionOf()) return false;
		Entity parent = node.asExtensionOf().entity();
		return parent.equals(expectedParent) || isDescendantOf(parent, expectedParent);
	}

	private String getTankEventName(String name) {
		return name.substring(name.lastIndexOf('.') + 1);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private void reflowTimelinesOf(MasterDatamart datamart, Timeline timeline, String measurementTank,
								   Datalake.Store.Source<MeasurementEvent> ss, MessageEventStream messageEvents) {
		try (OfSingleTimeline mounter = new OfSingleTimeline(datamart, timeline, measurementTank, ss.name()); messageEvents) {
			Stream messages = messageEvents.stream();
			List<MeasurementEvent> measurements = ss.tubs().flatMap(Datalake.Store.Tub::events).toList();
			Iterator<Event> events = EventStream.merge(Stream.of(messages, measurements.stream())).iterator();
			while (events.hasNext()) mounter.mount(events.next());
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	@SuppressWarnings("unchecked")
	private Iterator<Event> reflowTanks(Set<String>... tanks) {
		Set<String> tankNames = new HashSet<>();
		Arrays.stream(tanks).forEach(tankNames::addAll);
		return EventStream.merge(tanks(tankNames).map(tank -> (Stream<Event>) tank.content())).iterator();
	}

	private Iterator<Event> merge(Instant fromTs, Set<String> tankNames) {
		Timetag fromTimetag = Timetag.of(fromTs, Scale.Minute);
		return EventStream.merge(tanks(tankNames).map(tank -> (Stream<Event>) tank.content((ss, tt) -> tt.isAfter(fromTimetag))))
				.filter(e -> e.ts().isAfter(fromTs))
				.iterator();
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
				.flatMap(MounterUtils::tanksOf)
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

	private interface MessageEventStream extends Iterator<MessageEvent>, AutoCloseable {

		default Stream<MessageEvent> stream() {
			return StreamSupport.stream(Spliterators.spliteratorUnknownSize(this, Spliterator.SORTED), false);
		}

		class Reading implements MessageEventStream {

			private final ZimStream events;

			public Reading(File file) {
				try {
					this.events = ZimStream.of(file);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public void close() throws Exception {
				events.close();
			}

			@Override
			public boolean hasNext() {
				return events.hasNext();
			}

			@Override
			public MessageEvent next() {
				return new MessageEvent(events.next());
			}
		}

		class InMemory implements MessageEventStream {

			private final MessageEvent[] events;
			private int index;

			public InMemory(File file) {
				try {
					this.events = ZimStream.of(file).map(MessageEvent::new).toArray(MessageEvent[]::new);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public void close() throws Exception {
				index = 0;
			}

			@Override
			public boolean hasNext() {
				return index < events.length;
			}

			@Override
			public MessageEvent next() {
				return events[index++];
			}
		}

	}
}