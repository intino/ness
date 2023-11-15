package io.intino.datahub.datamart.impl;

import io.intino.alexandria.logger.Logger;
import io.intino.datahub.datamart.MasterDatamart;
import io.intino.datahub.model.Datalake;
import io.intino.datahub.model.Datamart;
import io.intino.sumus.chronos.ReelFile;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.intino.datahub.box.DataHubBox.REEL_EXTENSION;

class ReelDirectory extends MasterDatamart.ChronosDirectory<ReelFile> {

	private final Set<String> subscribedEvents;

	public ReelDirectory(Datamart definition, File root) {
		super(root);
		this.subscribedEvents = definition.reelList().stream()
				.map(r -> r.tank().message().name$())
				.collect(Collectors.toSet());
	}

	@Override
	protected String extension() {
		return REEL_EXTENSION;
	}

	@Override
	public ReelFile get(String type, String id) {
		try {
			return contains(type, id) ? ReelFile.open(fileOf(type, id)) : null;
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
	public Collection<String> subscribedEvents() {
		return subscribedEvents;
	}

	@Override
	public boolean isSubscribedTo(Datalake.Tank tank) {
		if (!tank.isMessage() || tank.asMessage() == null || tank.asMessage().message() == null) return false;
		return subscribedEvents().contains(tank.asMessage().message().name$());
	}
}
