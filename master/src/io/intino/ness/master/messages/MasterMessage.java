package io.intino.ness.master.messages;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public abstract class MasterMessage implements Serializable {

	private final String id = MasterMessageIdGenerator.generate(getClass());

	public final String id() {
		return id;
	}

	public abstract Instant ts();

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		MasterMessage that = (MasterMessage) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return MasterMessageSerializer.serialize(this);
	}
}
