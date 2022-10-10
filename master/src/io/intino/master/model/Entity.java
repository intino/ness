package io.intino.master.model;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public abstract class Entity {

	private final Id id;
	private final Map<String, String> extraAttributes = new HashMap<>(3);

	public Entity(String id) {
		this.id = new Id(id);
	}

	public Id id() {
		return id;
	}

	public Entity add(Triple t) {
		extraAttributes.put(t.predicate(), t.value());
		return this;
	}

	public Entity remove(Triple t) {
		extraAttributes.remove(t.predicate());
		return this;
	}

	public Map<String, String> extraAttributes() {
		return Collections.unmodifiableMap(extraAttributes);
	}

	public String extraAttribute(String name) {
		return extraAttributes.get(name);
	}

	public boolean hasExtraAttribute(String name) {
		return extraAttributes.containsKey(name);
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

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	public DateTimeFormatter dateFormatter() {
		return DATE_FORMATTER;
	}

	private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
	public DateTimeFormatter dateTimeFormatter() {
		return DATETIME_FORMATTER;
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
			return Triple.typeOf(id);
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
			return Objects.hash(id);
		}
	}
}
