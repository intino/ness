package io.intino.datahub.datamart.impl;

import io.intino.alexandria.logger.Logger;
import io.intino.datahub.datamart.MasterDatamart;
import io.intino.datahub.datamart.mounters.MounterUtils;
import io.intino.datahub.model.Datalake;
import io.intino.datahub.model.Datamart;
import io.intino.sumus.chronos.TimelineStore;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.intino.datahub.box.DataHubBox.TIMELINE_EXTENSION;

class TimelineDirectory extends MasterDatamart.ChronosDirectory<TimelineStore> {

	private final Set<String> subscribedEvents;

	public TimelineDirectory(Datamart definition, File root) {
		super(root);
		this.subscribedEvents = definition.timelineList().stream()
				.flatMap(MounterUtils::types)
				.collect(Collectors.toSet());
	}

	@Override
	protected String extension() {
		return TIMELINE_EXTENSION;
	}

	@Override
	public TimelineStore get(String type, String id) {
		try {
			return contains(type, id) ? TimelineStore.of(fileOf(type, id)) : null;
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	@Override
	public Stream<TimelineStore> stream() {
		return listFiles().stream().map(f -> {
			try {
				return TimelineStore.of(f);
			} catch (IOException e) {
				return null;
			}
		}).filter(Objects::nonNull);
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
