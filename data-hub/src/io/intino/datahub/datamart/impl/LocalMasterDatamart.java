package io.intino.datahub.datamart.impl;

import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.Message;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.datamart.MasterDatamart;
import io.intino.datahub.datamart.mounters.EntityMounter;
import io.intino.datahub.datamart.mounters.MasterDatamartMounter;
import io.intino.datahub.datamart.mounters.ReelMounter;
import io.intino.datahub.datamart.mounters.TimelineMounter;
import io.intino.datahub.model.Datalake;
import io.intino.datahub.model.Datamart;
import io.intino.datahub.model.Entity;
import io.intino.sumus.chronos.ReelFile;
import io.intino.sumus.chronos.TimelineFile;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.intino.datahub.box.DataHubBox.REEL_EXTENSION;
import static io.intino.datahub.box.DataHubBox.TIMELINE_EXTENSION;
import static java.util.Collections.emptyList;
import static java.util.Collections.synchronizedMap;

public class LocalMasterDatamart implements MasterDatamart {

	private final DataHubBox box;
	private final Datamart definition;
	private final File directory;
	private final Store<Message> entities;
	private final Store<TimelineFile> timelines;
	private final Store<ReelFile> reels;

	public LocalMasterDatamart(DataHubBox box, Datamart definition) {
		this.box = box;
		this.definition = definition;
		this.directory = box.datamartDirectory(definition.name$());
		this.entities = new EntityStore(definition);
		this.timelines = new TimelineStore(definition, new File(directory, "timelines"));
		this.reels = new ReelStore(definition, new File(directory, "reels"));
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
	public Store<TimelineFile> timelineStore() {
		return timelines;
	}

	@Override
	public Store<ReelFile> reelStore() {
		return reels;
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

	private static abstract class ChronosStore<T> implements Store<T> {

		private final File root;

		public ChronosStore(File root) {
			this.root = root;
		}

		protected abstract String extension();

		@Override
		public int size() {
			return listFiles().size();
		}

		@Override
		public boolean contains(String id) {
			return fileOf(id).exists();
		}

		@Override
		public void put(String id, T value) {

		}

		@Override
		public void remove(String id) {
			fileOf(id).delete();
		}

		protected File fileOf(String id) {
			return new File(root, id + extension());
		}

		protected List<File> listFiles() {
			File[] files = root.listFiles(f -> f.isFile() && f.getName().endsWith(extension()));
			return files == null ? emptyList() : Arrays.asList(files);
		}
	}

	private static class TimelineStore extends ChronosStore<TimelineFile> {

		private final Set<String> subscribedEvents;

		public TimelineStore(Datamart definition, File root) {
			super(root);
			this.subscribedEvents = definition.timelineList().stream()
					.flatMap(t -> Stream.of(t.entity().name$(), t.tank().sensor().name$()))
					.collect(Collectors.toSet());
		}

		@Override
		protected String extension() {
			return TIMELINE_EXTENSION;
		}

		@Override
		public TimelineFile get(String id) {
			try {
				return contains(id) ? TimelineFile.open(fileOf(id)) : null;
			} catch (IOException e) {
				Logger.error(e);
				return null;
			}
		}

		@Override
		public Stream<TimelineFile> stream() {
			return listFiles().stream().map(f -> {
				try {
					return TimelineFile.open(f);
				} catch (IOException e) {
					return null;
				}
			}).filter(Objects::nonNull);
		}

		@Override
		public Map<String, TimelineFile> toMap() {
			return stream().collect(Collectors.toMap(TimelineFile::id, Function.identity()));
		}

		@Override
		public Collection<String> subscribedEvents() {
			return subscribedEvents;
		}

		@Override
		public boolean isSubscribedTo(Datalake.Tank tank) {
			Collection<String> events = subscribedEvents();
			if (tank.isMeasurement() && events.contains(tank.asMeasurement().sensor().name$())) return true;
			return tank.isMessage() && events.contains(tank.asMessage().message().name$());
		}
	}

	private static class ReelStore extends ChronosStore<ReelFile> {

		private final Set<String> subscribedEvents;

		public ReelStore(Datamart definition, File root) {
			super(root);
			this.subscribedEvents = definition.reelList().stream()
					.flatMap(r -> r.signalList().stream().map(s -> s.tank().message().name$()))
					.collect(Collectors.toSet());
		}

		@Override
		protected String extension() {
			return REEL_EXTENSION;
		}

		@Override
		public ReelFile get(String id) {
			try {
				return contains(id) ? ReelFile.open(fileOf(id)) : null;
			} catch (IOException e) {
				Logger.error(e);
				return null;
			}
		}

		@Override
		public Stream<ReelFile> stream() {
			return listFiles().stream().map(f -> {
				try {
					return ReelFile.open(f);
				} catch (IOException e) {
					return null;
				}
			}).filter(Objects::nonNull);
		}

		@Override
		public Map<String, ReelFile> toMap() {
			return listFiles().stream().filter(this::isValidReelFile).collect(Collectors.toMap(
					f -> f.getName().replace(extension(), ""),
					f -> {
						try {
							return ReelFile.open(f);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}));
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

		private boolean isValidReelFile(File file) {
			try {
				ReelFile.open(file);
				return true;
			} catch (IOException e) {
				return false;
			}
		}
	}
}
