package io.intino.ness.inl;

import java.lang.reflect.Field;
import java.util.List;

import static java.lang.reflect.Modifier.isStatic;
import static java.lang.reflect.Modifier.isTransient;

public class Serializer {
	private final Object object;
	private final String path;
	private final Accessory.Mapping mapping;

	public static Serializer serialize(Object object) {
		return new Serializer(object, "", new Accessory.Mapping());
	}

	private Serializer(Object object, String path, Accessory.Mapping mapping) {
		this.object = object;
		this.path = path;
		this.mapping = mapping;
	}

	public String toInl() {
		return object instanceof List ? toInl((List) object) : header() + body();
	}

	private String toInl(List list) {
		String result = "";
		for (Object o : list)
			result += "\n" + serialize(o).toInl();
		return result.substring(1);
	}

	private String header() {
		return "[" + type() + "]" + "\n";
	}

	private String type() {
		return path + (path.isEmpty() ? "" : ".") + map(className());
	}

	private String map(String text) {
		return mapping.get(text);
	}

	private String body() {
		return serializeAttributes() + serializeComponents();
	}

	private String serializeAttributes() {
		String result = "";
		for (Field field : Accessory.fieldsOf(object).asList()) {
			if (isTransient(field.getModifiers())) continue;
			if (isStatic(field.getModifiers())) continue;
			if (!isAttribute(field)) continue;
			if (valueOf(field) == null || isEmpty(field)) continue;
			result += serializeAttribute(field) + "\n";
		}
		return result;
	}

	private String serializeAttribute(Field field) {
		return map(field.getName()) + separatorFor(serializeAttributeValue(valueOf(field)));
	}

	private String serializeAttributeValue(Object value) {
		return value != null ? formatterOf(value).format(value) : "";
	}

	private String separatorFor(String value) {
		return value.startsWith("\n") ? ":" + value : ": " + value;
	}

	private Accessory.Formatter formatterOf(Object value) {
		return Accessory.formatters.get(value.getClass());
	}

	private String serializeComponents() {
		String result = "";
		for (Field field : Accessory.fieldsOf(object).asList()) {
			if (isTransient(field.getModifiers())) continue;
			if (isAttribute(field.getType())) continue;
			result += serializeComponent(field);
		}
		return result;
	}

	private String serializeComponent(Field field) {
		Object object = valueOf(field);
		if (object == null) return "";
		return isList(field) ? serializeTable((List) object) : serializeItem(object);
	}

	private String serializeTable(List list) {
		String result = "";
		for (Object item : list)
			result += serializeItem(item);
		return result;
	}

	private String serializeItem(Object value) {
		return "\n" + new Serializer(value, type(), mapping).toInl();
	}

	private boolean isAttribute(Field field) {
		return isAttribute(field.getType());
	}

	private boolean isAttribute(Class<?> aClass) {
		return aClass.getName().startsWith("java.lang") ||
				aClass.getName().startsWith("java.util.Date") ||
				aClass.getName().startsWith("java.time") ||
				aClass.isPrimitive() ||
				aClass.isArray();
	}

	private boolean isEmpty(Field field) {
		Object value = valueOf(field);
		return value.getClass().isArray() && ((Object[]) value).length == 0;
	}


	private boolean isList(Field field) {
		return field.getType().isAssignableFrom(List.class);
	}

	private String className() {
		return object.getClass().getSimpleName();
	}

	private Object valueOf(Field field) {
		try {
			field.setAccessible(true);
			return field.get(object);
		} catch (IllegalAccessException e) {
			return null;
		}
	}

	public Serializer map(String from, String to) {
		mapping.put(from, to);
		return this;
	}
}
