package io.intino.datahub.datamart.impl;

import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.Message;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.datamart.MasterDatamart;
import io.intino.datahub.datamart.TimeShiftCache;
import io.intino.datahub.datamart.mounters.EntityMounter;
import io.intino.datahub.datamart.mounters.MasterDatamartMounter;
import io.intino.datahub.datamart.mounters.ReelMounter;
import io.intino.datahub.datamart.mounters.timelines.TimelineMounter;
import io.intino.datahub.model.Datalake;
import io.intino.datahub.model.Datamart;
import io.intino.datahub.model.Entity;
import io.intino.sumus.chronos.ReelFile;
import io.intino.sumus.chronos.TimelineStore;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.intino.datahub.datamart.MasterDatamart.normalizePath;
import static java.util.Collections.synchronizedMap;

public class LocalMasterDatamart implements MasterDatamart {
	private final DataHubBox box;
	private final Datamart definition;
	private final File directory;
	private final Store<Message> entities;
	private final ChronosDirectory<TimelineStore> timelines;
	private final ChronosDirectory<ReelFile> reels;
	private final IndicatorDirectory indicators;
	private final Map<String, TimeShiftCache> caches;

	public LocalMasterDatamart(DataHubBox box, Datamart definition) {
		this.box = box;
		this.definition = definition;
		this.directory = box.datamartDirectory(definition.name$());
		this.entities = new EntityStore(definition);
		this.timelines = new TimelineDirectory(definition, new File(directory, "timelines"));
		this.indicators = new IndicatorDirectory(definition, new File(directory, "indicators"));
		this.reels = new ReelDirectory(definition, new File(directory, "reels"));
		this.caches = new HashMap<>();
	}

	@Override
	public Datamart definition() {
		return definition;
	}

	public File directory() {
		return directory;
	}

	@Override
	public DataHubBox box() {
		return box;
	}

	@Override
	public String name() {
		return definition.name$();
	}

	@Override
	public Store<Message> entityStore() {
		return entities;
	}

	@Override
	public ChronosDirectory<TimelineStore> timelineStore() {
		return timelines;
	}

	@Override
	public IndicatorDirectory indicatorStore() {
		return indicators;
	}

	@Override
	public ChronosDirectory<ReelFile> reelStore() {
		return reels;
	}

	public synchronized TimeShiftCache cacheOf(String timeline) {
		if (!caches.containsKey(timeline)) {
			File dir = new File(box.datamartsDirectory(), ".cache");
			dir.mkdirs();
			caches.put(timeline, new TimeShiftCache(new File(dir, normalizePath(timeline) + ".db")).open());
		}
		return caches.get(timeline);
	}

	@Override
	public void close() {
		caches.values().forEach(c -> {
			try {
				c.close();
			} catch (Exception e) {
				Logger.error(e);
			}
		});
	}

	@Override
	public Stream<MasterDatamartMounter> createMountersFor(Datalake.Tank tank) {
		if (tank.isMeasurement()) return Stream.of(new TimelineMounter(this));
		if (!tank.isMessage()) return Stream.empty();
		List<MasterDatamartMounter> mounters = new ArrayList<>(2);
		if (entityStore().isSubscribedTo(tank)) mounters.add(new EntityMounter(this));
		if (timelineStore().isSubscribedTo(tank)) mounters.add(new TimelineMounter(this));
		if (reelStore().isSubscribedTo(tank)) mounters.add(new ReelMounter(this));
		return mounters.stream();
	}

	public LocalMasterDatamart reflow(Stream<Message> messages) {
		try (messages) {
			messages.forEach(m -> {
				String id = m.get("id").asString();
				if (id != null && !id.isBlank())
					entities.put(id, m);
			});
		}
		return this;
	}

	private static class EntityStore implements MasterDatamart.Store<Message> {

		private final Map<String, Message> entities;
		private final Set<String> subscribedEvents;

		public EntityStore(Datamart definition) {
			this.entities = new ConcurrentHashMap<>(1024);
			this.subscribedEvents = definition.entityList().stream()
					.map(Entity::from)
					.filter(Objects::nonNull)
					.map(m -> m.message().name$())
					.collect(Collectors.toSet());
		}

		public EntityStore(Datamart definition, Stream<Message> messages) {
			this.entities = synchronizedMap(messages.filter(m -> m.contains("id")).collect(Collectors.toMap(m -> m.get("id").asString(), Function.identity())));
			this.subscribedEvents = definition.entityList().stream()
					.map(Entity::from)
					.filter(Objects::nonNull)
					.map(m -> m.message().name$())
					.collect(Collectors.toSet());
		}

		@Override
		public int size() {
			return entities.size();
		}

		@Override
		public boolean contains(String id) {
			return entities.containsKey(id);
		}

		@Override
		public Message get(String id) {
			return entities.get(id);
		}

		@Override
		public void put(String id, Message value) {
			entities.put(id, value);
		}

		@Override
		public void remove(String id) {
			entities.remove(id);
		}

		@Override
		public void clear() {
			entities.clear();
		}

		@Override
		public Stream<Message> stream() {
			return entities.values().stream();
		}

		@Override
		public Map<String, Message> toMap() {
			return entities;
		}

		@Override
		public Collection<String> subscribedEvents() {
			return subscribedEvents;
		}

		@Override
		public boolean isSubscribedTo(Datalake.Tank tank) {
			if (!tank.isMessage() || tank.asMessage() == null || tank.asMessage().message() == null) return false;
			return subscribedEvents().contains(tank.asMessage().message().name$());
		}
	}

}
