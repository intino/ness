package io.intino.ness.inl;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.reflect.Array.set;
import static java.util.Arrays.asList;

public class Accessory {

	public interface Parser {
		Object parse(String text);
	}

	public interface Formatter {
		String format(Object value);
	}

	public static final Map<Class, Formatter> formatters = new HashMap<>();
	public static final Map<Class, Parser> parsers = new HashMap<>();
	private static final DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
    private static final String NullValue = "\0";

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

	static String unwrap(String text) {
		return text.substring(1, text.length() - 1);
	}

	static boolean isMultilineIn(String line) {
		return line.startsWith("\t");
	}

	static boolean isMessageIn(String line) {
		return line.startsWith("[");
	}

	static boolean isAttributeIn(String line) {
		return line.contains(":");
	}

	static class FieldQuery extends Accessory {
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

	private static String formatText(Object text) {
		return text == null ? NullValue : text.toString().contains("\n") ? "\n\t" + text.toString().replaceAll("\n", "\n\t") : text.toString();
	}

	private static Date parseDate(final String text) {
		try {
			return dateFormatter.parse(text);
		} catch (ParseException e) {
			return null;
		}
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
        String version = System.getProperty("java.version");
        Class<?> instantClass = instantClass();
        if (version.startsWith("1.8") && instantClass != null) {
            formatters.put(instantClass, instantFormatter());
            parsers.put(instantClass, instantParserOf(instantClass));
        }
    }

    private static Class<?> instantClass() {
        try {
            return Class.forName("java.time.Instant");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Parser instantParserOf(Class<?> instantClass)  {
        try {
            final Method method = instantClass.getDeclaredMethod("parse", CharSequence.class);
            return new Parser() {
                @Override
                public Object parse(String text)  {
                    try {
                        return method.invoke(null, text);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            };
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Formatter instantFormatter() {
        return new Formatter() {
            @Override
            public String format(Object value) {
                return value.toString();
            }
        };
    }


}
