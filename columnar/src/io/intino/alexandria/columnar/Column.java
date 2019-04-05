package io.intino.alexandria.columnar;

import java.util.List;

public class Column {

	private final String name;
	private final ColumnType type;
	private final Mapper mapper;

	public Column(String name, ColumnType type, Mapper mapper) {
		this.name = name;
		this.type = type;
		this.mapper = mapper;
	}

	public Column(String name, ColumnType type) {
		this.name = name;
		this.type = type;
		this.mapper = values -> values.isEmpty() ? null : values.get(0);
	}

	public String name() {
		return name;
	}

	public ColumnType type() {
		return type;
	}

	public Mapper mapper() {
		return mapper;
	}

	public String map(List<String> values) {
		Object object = mapper.map(values);
		return object != null ? object.toString() : null;
	}

	@Override
	public String toString() {
		return name;
	}

	public interface Mapper {
		Object map(List<String> values);
	}

	public static abstract class ColumnType {

		public static Date date(java.lang.String format) {
			return new Date(format);
		}

		public static String string() {
			return new String();
		}

		public static Nominal nominal(java.lang.String[] values) {
			return new Nominal(values);
		}

		public static Numeric numeric() {
			return new Numeric();
		}


		public static class Date extends ColumnType {
			private final java.lang.String format;

			public Date(java.lang.String format) {
				this.format = format;
			}

			public java.lang.String format() {
				return format;
			}
		}

		public static class Nominal extends ColumnType {
			private final java.lang.String[] values;

			public Nominal(java.lang.String[] values) {
				this.values = values;
			}

			public java.lang.String[] values() {
				return values;
			}
		}

		public static class Numeric extends ColumnType {

		}

		public static class String extends ColumnType {

		}
	}
}
