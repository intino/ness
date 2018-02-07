package io.intino.ness.inl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import static io.intino.ness.inl.Formatters.Formatter;
import static io.intino.ness.inl.Parsers.Parser;
import static java.util.Arrays.asList;

@SuppressWarnings("ALL")
public class Accessory {

	static class Mapping {

		private Map<String, String> map = new HashMap<>();

		public void put(String from, String to) {
			map.put(from.toLowerCase(), to);
		}

		public String get(String text) {
			return map.containsKey(text.toLowerCase()) ? map.get(text.toLowerCase()) : text;
		}
	}

	static FieldQuery fieldsOf(Object object) {
		return new FieldQuery(object);
	}

	static List<Field> fieldsOf(Class type) {
		if (type == null) return new ArrayList<>();
		List<Field> list = fieldsOf(type.getSuperclass());
		list.addAll(asList(type.getDeclaredFields()));
		return list;
	}

	static String normalize(String line) {
		if (line == null) return null;
		if (line.startsWith("\t")) return line;
		line = line.trim();
		if (line.isEmpty()) return line;
		if (line.startsWith("[")) return line;
		return line.replaceAll("(\\w*)\\s*[:=]\\s*(.*)", "$1:$2");
	}

	static class FieldQuery {
		private final Object object;

		FieldQuery(Object object) {
			this.object = object;
		}

		List<Field> asList() {
			return fieldsOf(object.getClass());
		}

		Field get(String name) {
			for (Field field : fieldsOf(object.getClass()))
				if (name.equalsIgnoreCase(field.getName())) return field;
			throw new RuntimeException(name + " attribute doesn't exist");
		}
	}

	static {
		String version = System.getProperty("java.version");
		Class<?> instantClass = instantClass();
		if (version.startsWith("1.8") && instantClass != null) {
			Formatters.put(instantClass, instantFormatter());
			Parsers.put(instantClass, instantParserOf(instantClass));
		}
	}

	private static Class<?> instantClass() {
		try {
			return Class.forName("java.time.Instant");
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	private static Parsers.Parser instantParserOf(Class<?> instantClass) {
		try {
			final Method method = instantClass.getDeclaredMethod("read", CharSequence.class);
			return new Parser() {
				@Override
				public Object parse(String text) {
					try {
						return method.invoke(null, text);
					} catch (Throwable e) {
						return simpleDateFormatter(text);
					}
				}
			};
		} catch (NoSuchMethodException e) {
			return null;
		}
	}

	private static Object simpleDateFormatter(String text) {
		try {
			Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(text);
			Method toInstant = Date.class.getDeclaredMethod("toInstant");
			try {
				return toInstant.invoke(date, text);
			} catch (Throwable e) {
				return null;
			}
		} catch (ParseException | NoSuchMethodException e) {
			return null;
		}
	}

	private static Formatters.Formatter instantFormatter() {
		return new Formatter() {
			@Override
			public String format(Object value) {
				return value.toString();
			}
		};
	}


}
