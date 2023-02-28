package io.intino.ness.master;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public abstract class Entity implements Serializable {

	private final String id;
	private boolean enabled = true;
	private transient MasterDatamart datamart;

	public Entity(String id) {
		if(id == null) throw new NullPointerException("Entity id cannot be null!!");
		this.id = id;
	}

	public String id() {
		return id;
	}

	public boolean enabled() {
		return enabled;
	}

	public Entity enabled(boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	public MasterDatamart datamart() {
		return datamart;
	}

	Entity datamart(MasterDatamart datamart) {
		this.datamart = requireNonNull(datamart);
		return this;
	}

	public abstract List<Attribute> attributes();

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Entity entity = (Entity) o;
		return Objects.equals(id, entity.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		return id;
	}

	public static class Attribute {

		private final String name;
		private final Class<?> type;
		private final Object value;

		public Attribute(String name, Class<?> type, Object value) {
			this.name = name;
			this.type = type;
			this.value = value;
		}

		public String name() {
			return name;
		}

		public Class<?> type() {
			return type;
		}

		public Object value() {
			return value;
		}

		public <T> T valueAs(Class<T> type) {
			return type.cast(value);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Attribute attribute = (Attribute) o;
			return Objects.equals(name, attribute.name) && Objects.equals(type, attribute.type) && Objects.equals(value, attribute.value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, type, value);
		}

		@Override
		public String toString() {
			return type.getSimpleName() + " " + name + " = " + value;
		}
	}
}