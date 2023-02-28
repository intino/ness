package io.intino.ness.master.datamarts;

import io.intino.ness.master.Entity;
import io.intino.ness.master.MasterDatamart;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;


public class HashMapMasterDatamart implements MasterDatamart {

	private final String name;
	private final Map<String, Entity> entities;
	private Instant lastUpdate;

	public HashMapMasterDatamart(String name) {
		this.name = name;
		this.entities = new HashMap<>();
	}

	@Override
	public int size() {
		return entities.size();
	}

	@Override
	public boolean contains(String entityId) {
		return entities.containsKey(entityId);
	}

	@Override
	public Entity get(String entityId) {
		return entities.get(entityId);
	}

	@Override
	public void put(String name, Entity entity) {
		entities.put(name, setEntityDatamart(entity));
		lastUpdate = Instant.now();
	}

	@Override
	public void remove(String entityId) {
		entities.remove(entityId);
		lastUpdate = Instant.now();
	}

	@Override
	public void clear() {
		entities.clear();
		lastUpdate = Instant.now();
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public Map<String, Entity> toMap() {
		return Collections.unmodifiableMap(entities);
	}

	@Override
	public Stream<Entity> entities() {
		return entities.values().stream();
	}

	@Override
	public Optional<Instant> lastUpdate() {
		return Optional.ofNullable(lastUpdate);
	}

	@Override
	public String toString() {
		return name + " (" + entities.size() + ")";
	}
}
