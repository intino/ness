package io.intino.ness.master.messages;

import io.intino.alexandria.Json;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public abstract class MasterMessage implements Serializable {

	private final String id;
	private final Instant ts;

	public MasterMessage() {
		this.id = MasterMessageIdGenerator.generateFor(getClass());
		this.ts = Instant.now();
	}

	public final String id() {
		return id;
	}

	public Instant ts() {
		return ts;
	}

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
		return Json.toJsonPretty(this);
	}
}
