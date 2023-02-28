package io.intino.ness.master.datamarts;

import io.intino.ness.master.Entity;
import io.intino.ness.master.MasterDatamart;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class SynchronizedMasterDatamart implements MasterDatamart {

	private final Object mutex = new Object();
	private final MasterDatamart datamart;

	public SynchronizedMasterDatamart(MasterDatamart datamart) {
		this.datamart = requireNonNull(datamart);
	}

	@Override
	public int size() {
		synchronized (mutex) {
			return datamart.size();
		}
	}

	@Override
	public boolean contains(String entityId) {
		synchronized (mutex) {
			return datamart.contains(entityId);
		}
	}

	@Override
	public Entity get(String entityId) {
		synchronized (mutex) {
			return datamart.get(entityId);
		}
	}

	@Override
	public void put(String name, Entity entity) {
		synchronized (mutex) {
			datamart.put(name, entity);
		}
	}

	@Override
	public void remove(String entityId) {
		synchronized (mutex) {
			datamart.remove(entityId);
		}
	}

	@Override
	public void clear() {
		synchronized (mutex) {
			datamart.clear();
		}
	}

	@Override
	public String name() {
		return datamart.name();
	}

	@Override
	public Map<String, Entity> toMap() {
		synchronized (mutex) {
			return datamart.toMap();
		}
	}

	@Override
	public Stream<Entity> entities() {
		synchronized (mutex) {
			return datamart.entities();
		}
	}

	@Override
	public Optional<Instant> lastUpdate() {
		synchronized (mutex) {
			return datamart.lastUpdate();
		}
	}

	public MasterDatamart internalDatamart() {
		return datamart;
	}
}
