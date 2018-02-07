package io.intino.ness.inl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Formatters {

	private static final DateFormat dateFormatter;
	private static final String NullValue = "\0";
	private static final Map<Class, Formatter> formatters = new HashMap<>();

	public static Formatter get(Class<?> aClass) {
		return formatters.get(aClass);
	}

	public static void put(Class<?> aClass, Formatter formatter) {
		formatters.put(aClass, formatter);
	}


	public interface Formatter {
		String format(Object value);
	}


	private static class ArrayFormatter {
		private Formatter formatter;

		ArrayFormatter(Formatter formatter) {
			this.formatter = formatter;
		}

		static ArrayFormatter of(Class type) {
			return new ArrayFormatter(formatters.get(type));
		}

		String format(Object o) {
			String result = "";
			for (Object item : (Object[]) o)
				result += "\n\t" + (item == null ? NullValue : formatter.format(item));
			return result;
		}
	}


	static {
		dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
		//dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	static {
		formatters.put(Boolean.class, new Formatter() {
			public String format(Object data) {
				return data.toString();
			}
		});
		formatters.put(Byte.class, new Formatter() {
			public String format(Object data) {
				return data.toString();
			}
		});
		formatters.put(Integer.class, new Formatter() {
			public String format(Object data) {
				return data.toString();
			}
		});
		formatters.put(Float.class, new Formatter() {
			public String format(Object data) {
				return data.toString();
			}
		});
		formatters.put(Double.class, new Formatter() {
			public String format(Object data) {
				return data.toString();
			}
		});
		formatters.put(String.class, new Formatter() {
			public String format(Object text) {
				return formatText(text);
			}
		});
		formatters.put(Date.class, new Formatter() {
			public String format(Object date) {
				return dateFormatter.format(date);
			}
		});
		formatters.put(Boolean[].class, new Formatter() {
			public String format(Object array) {
				return ArrayFormatter.of(Boolean.class).format(array);
			}
		});
		formatters.put(Byte[].class, new Formatter() {
			public String format(Object array) {
				return ArrayFormatter.of(Byte.class).format(array);
			}
		});
		formatters.put(Integer[].class, new Formatter() {
			public String format(Object array) {
				return ArrayFormatter.of(Integer.class).format(array);
			}
		});
		formatters.put(Float[].class, new Formatter() {
			public String format(Object array) {
				return ArrayFormatter.of(Float.class).format(array);
			}
		});
		formatters.put(Double[].class, new Formatter() {
			public String format(Object array) {
				return ArrayFormatter.of(Double.class).format(array);
			}
		});
		formatters.put(String[].class, new Formatter() {
			public String format(Object array) {
				return ArrayFormatter.of(String.class).format(array);
			}
		});
		formatters.put(Date[].class, new Formatter() {
			public String format(Object array) {
				return ArrayFormatter.of(Date.class).format(array);
			}
		});
	}

	private static String formatText(Object o) {
		if (o == null) return NullValue;
		String text = o.toString();
		return text.contains("\n") ? "\n\t" + text.replaceAll("\n", "\n\t") : text;
	}



}
