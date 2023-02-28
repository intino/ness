package io.intino.ness.master;

import io.intino.ness.master.datamarts.SynchronizedMasterDatamart;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public interface MasterDatamart extends Serializable {

	int size();

	boolean contains(String entityId);

	Entity get(String entityId);

	void put(String name, Entity entity);

	void remove(String entityId);

	void clear();

	String name();

	Map<String, Entity> toMap();

	Stream<Entity> entities();

	Optional<Instant> lastUpdate();

	default Entity setEntityDatamart(Entity entity) {
		return entity.datamart(this);
	}

	default void init() {
		entities().forEach(this::setEntityDatamart);
	}

	static MasterDatamart synchronizedDatamart(MasterDatamart datamart) {
		return new SynchronizedMasterDatamart(datamart);
	}
}
