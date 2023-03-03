package io.intino.ness.datahubterminalplugin;


import io.intino.itrules.Formatter;
import io.intino.itrules.Template;
import io.intino.itrules.formatters.StringFormatters;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Formatters {
	public static Formatter validName() {
		return (value) -> snakeCaseToCamelCase(value.toString().replace(".", "-"));
	}

	public static Formatter snakeCaseToCamelCase() {
		return value -> snakeCaseToCamelCase(value.toString());
	}

	public static Formatter camelCaseToSnakeCase() {
		return value -> camelCaseToSnakeCase(value.toString());
	}

	public static Formatter returnType() {
		return value -> value.equals("Void") ? "void" : value;
	}

	public static String firstLowerCase(String value) {
		return value.substring(0, 1).toLowerCase() + value.substring(1);
	}

	public static String firstUpperCase(String value) {
		return value.substring(0, 1).toUpperCase() + value.substring(1);
	}

	public static Formatter returnTypeFormatter() {
		return value -> {
			if (value.equals("Void")) return "void";
			else if (value.toString().contains(".")) return firstLowerCase(value.toString());
			else return value;
		};
	}

	public static Formatter quoted() {
		return value -> '"' + value.toString() + '"';
	}

	public static Formatter validPackage() {
		return value -> value.toString().replace("-", "").toLowerCase();
	}

	private static Formatter subPath() {
		return value -> {
			final String path = value.toString();
			return path.contains(":") ? path.substring(0, path.indexOf(":")) : path;
		};
	}

	public static Formatter shortType() {
		return value -> {
			String type = value.toString();
			final String[] s = type.split("\\.");
			return s[s.length - 1];
		};
	}

	public static Template customize(Template template) {
		template.add("validname", validName());
		template.add("snakeCaseToCamelCase", snakeCaseToCamelCase());
		template.add("camelCaseToSnakeCase", camelCaseToSnakeCase());
		template.add("returnType", returnType());
		template.add("returnTypeFormatter", returnTypeFormatter());
		template.add("quoted", quoted());
		template.add("validPackage", validPackage());
		template.add("subpath", subPath());
		template.add("shortType", shortType());
		template.add("quoted", quoted());
		template.add("customParameter", customParameter());
		return template;
	}

	private static Formatter customParameter() {
		return value -> value.toString().substring(1, value.toString().length() - 1);
	}

	private static String snakeCaseToCamelCase(String string) {
		if (string.isEmpty()) return string;
		else {
			String[] value = string.replace("_", "-").split("-");
			return Arrays.stream(value).map(part -> Character.toUpperCase(part.charAt(0)) + part.substring(1)).collect(Collectors.joining());
		}
	}

	private static String camelCaseToSnakeCase(String string) {
		if (string.isEmpty()) return string;
		return IntStream.range(1, string.length()).mapToObj(i -> String.valueOf(Character.isUpperCase(string.charAt(i)) ? "-" + Character.toLowerCase(string.charAt(i)) : string.charAt(i))).collect(Collectors.joining("", String.valueOf(Character.toLowerCase(string.charAt(0))), ""));
	}

	public static Formatter javaValidName() {
		return (s) -> {
			String value = s.toString();
			return javaValidWord().format(toCamelCaseWithoutFirstChange(value, "-"));
		};
	}

	private static Object toCamelCaseWithoutFirstChange(String value, String regex) {
		if (value.isEmpty()) {
			return "";
		} else {
			String[] parts = value.split(regex);
			if (parts.length == 1) {
				return value;
			} else {
				StringBuilder caseString = new StringBuilder(parts[0]);

				for(int i = 1; i < parts.length; ++i) {
					caseString.append(capitalize(parts[i]));
				}

				return caseString.toString();
			}
		}
	}

	public static Formatter javaValidWord() {
		return (s) -> {
			String value = s.toString();
			return JavaKeywords.isKeyword(value) ? value + "$" : value;
		};
	}

	public static String capitalize(String value) {
		return value.isEmpty() ? "" : ((Formatter) StringFormatters.get(Locale.getDefault()).get("capitalize")).format(value).toString();
	}
}