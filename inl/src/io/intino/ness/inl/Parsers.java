package io.intino.ness.inl;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.reflect.Array.set;

@SuppressWarnings("Duplicates")
public class Parsers {

	private static final DateFormat dateFormatter;
	private static final String NullValue = "\0";
	private static final Map<Class, Parser> parsers = new HashMap<>();

	public static Parser get(Class<?> aClass) {
		return parsers.get(aClass);
	}

	public static void put(Class<?> aClass, Parser parser) {
		parsers.put(aClass, parser);
	}

	public interface Parser {
		Object parse(String text);
	}


	private static class ArrayParser {
		private Class type;
		private Parser parser;

		ArrayParser(Class type, Parser parser) {
			this.type = type;
			this.parser = parser;
		}

		static ArrayParser of(Class type) {
			return new ArrayParser(type, parsers.get(type));
		}

		Object parse(String text) {
			String[] lines = text.split("\n");
			Object result = Array.newInstance(type, lines.length);
			for (int i = 0; i < lines.length; i++) {
				set(result, i, (NullValue.equals(lines[i]) ? null : parser.parse(lines[i])));
			}
			return result;
		}
	}

	static {
		dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
		dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	static {
		parsers.put(boolean.class, new Parser() {
			public Object parse(String text) {
				return Boolean.parseBoolean(text);
			}
		});
		parsers.put(byte.class, new Parser() {
			public Object parse(String text) {
				return Byte.parseByte(text);
			}
		});
		parsers.put(int.class, new Parser() {
			public Object parse(String text) {
				return Integer.parseInt(text);
			}
		});
		parsers.put(float.class, new Parser() {
			public Object parse(String text) {
				return Float.parseFloat(text);
			}
		});
		parsers.put(double.class, new Parser() {
			public Object parse(String text) {
				return Double.parseDouble(text);
			}
		});
		parsers.put(Boolean.class, parsers.get(boolean.class));
		parsers.put(Byte.class, parsers.get(byte.class));
		parsers.put(Integer.class, parsers.get(int.class));
		parsers.put(Float.class, parsers.get(float.class));
		parsers.put(Double.class, parsers.get(double.class));
		parsers.put(String.class, new Parser() {
			public Object parse(String text) {
				return text;
			}
		});
		parsers.put(Date.class, new Parser() {
			public Object parse(String text) {
				return parseDate(text);
			}
		});
		parsers.put(Boolean[].class, new Parser() {
			public Object parse(String text) {
				return ArrayParser.of(Boolean.class).parse(text);
			}
		});
		parsers.put(Byte[].class, new Parser() {
			public Object parse(String text) {
				return ArrayParser.of(Byte.class).parse(text);
			}
		});
		parsers.put(Integer[].class, new Parser() {
			public Object parse(String text) {
				return ArrayParser.of(Integer.class).parse(text);
			}
		});
		parsers.put(Float[].class, new Parser() {
			public Object parse(String text) {
				return ArrayParser.of(Float.class).parse(text);
			}
		});
		parsers.put(Double[].class, new Parser() {
			public Object parse(String text) {
				return ArrayParser.of(Double.class).parse(text);
			}
		});
		parsers.put(String[].class, new Parser() {
			public Object parse(String text) {
				return ArrayParser.of(String.class).parse(text);
			}
		});
		parsers.put(Date[].class, new Parser() {
			public Object parse(String text) {
				return ArrayParser.of(Date.class).parse(text);
			}
		});
	}

	private static Date parseDate(final String text) {
		try {
			return dateFormatter.parse(text);
		} catch (ParseException e) {
			return null;
		}
	}


}
