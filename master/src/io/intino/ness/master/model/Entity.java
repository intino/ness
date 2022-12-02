package io.intino.ness.master.model;

import java.util.*;

import static java.util.Objects.requireNonNull;

public abstract class Entity {

	private final Id id;
	private final Map<String, Triplet> extraTriplets = new HashMap<>(1);

	public Entity(String id) {
		this.id = new Id(id);
		extraTriplets.put("enabled", new Triplet(id, "enabled", "true"));
	}

	public Id id() {
		return id;
	}

	public boolean enabled() {
		return "true".equalsIgnoreCase(getExtraAttribute("enabled"));
	}

	private void setEnabled(boolean enabled) {
		final boolean oldValue = this.enabled();
		if(oldValue == enabled) return;

		if(enabled)
			onEnable();
		else
			onDisable();
	}

	protected void onEnable() {}
	protected void onDisable() {}

	public Entity add(Triplet t) {
		if(t.predicate().equals("enabled")) setEnabled(parseBoolean(t.value()));
		extraTriplets.put(t.predicate(), t);
		return this;
	}

	private boolean parseBoolean(String value) {
		return value.trim().equalsIgnoreCase("true");
	}

	public Entity remove(Triplet t) {
		extraTriplets.remove(t.predicate());
		return this;
	}

	public List<Triplet> triplets() {
		return new ArrayList<>(extraTriplets.values());
	}

	public TripletRecord asTripletRecord() {
		return new TripletRecord(id.id, triplets());
	}

	public Map<String, Triplet> extraTriplets() {
		return Collections.unmodifiableMap(extraTriplets);
	}

	public Triplet getExtraTriplet(String predicate) {
		return extraTriplets.get(predicate);
	}

	public boolean hasExtraAttribute(String predicate) {
		return extraTriplets.containsKey(predicate);
	}

	public String getExtraAttribute(String predicate) {
		return hasExtraAttribute(predicate) ? getExtraTriplet(predicate).value() : null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Entity entity = (Entity) o;
		return Objects.equals(id, entity.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return id.toString();
	}

	public static final class Id {

		private final String id;

		public Id(String id) {
			this.id = requireNonNull(id);
		}

		public String get() {
			return id;
		}

		public String value() {
			final int index = id.indexOf(':');
			return index < 0 ? id : id.substring(0, index);
		}

		public String type() {
			return Triplet.typeOf(id);
		}

		@Override
		public String toString() {
			return get();
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Id id1 = (Id) o;
			return Objects.equals(id, id1.id);
		}

		@Override
		public int hashCode() {
			return id.hashCode();
		}
	}
}
