package io.intino.ness.inl;

import java.lang.reflect.Field;
import java.util.List;

import static io.intino.ness.inl.Formatters.*;
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
		StringBuilder result = new StringBuilder();
		for (Object o : list)
			result.append("\n").append(serialize(o).toInl());
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
		StringBuilder result = new StringBuilder();
		for (Field field : Accessory.fieldsOf(object).asList()) {
			if (isTransient(field.getModifiers())) continue;
			if (isStatic(field.getModifiers())) continue;
			if (!isAttribute(field)) continue;
			if (valueOf(field) == null || isEmpty(field)) continue;
			result.append(serializeAttribute(field)).append("\n");
		}
		return result.toString();
	}

	private String serializeAttribute(Field field) {
		return map(field.getName()) + separatorFor(serializeAttributeValue(valueOf(field)));
	}

	private String serializeAttributeValue(Object value) {
		if (value == null) return "";
		return value instanceof List ? serializeAttributeValue(toArray((List) value)) : formatterOf(value).format(value);
	}

	private String separatorFor(String value) {
		return value.startsWith("\n") ? ":" + value : ": " + value;
	}

	private Formatter formatterOf(Object value) {
		return Formatters.get(value.getClass());
	}

	@SuppressWarnings("unchecked")
	private <T> T[] toArray(List<T> list) {
		T[] result = (T[]) java.lang.reflect.Array.newInstance(list.get(0).getClass(), list.size());
		for (int i = 0; i < list.size(); i++) {
			result[i] = list.get(i);
		}
		return result;
	}

	private String serializeComponents() {
		StringBuilder result = new StringBuilder();
		for (Field field : Accessory.fieldsOf(object).asList()) {
			if (isTransient(field.getModifiers())) continue;
			if (isStatic(field.getModifiers())) continue;
			if (valueOf(field) == null || isEmpty(field)) continue;
			if (isAttribute(field)) continue;
			result.append(serializeComponent(field));
		}
		return result.toString();
	}

	private String serializeComponent(Field field) {
		Object object = valueOf(field);
		if (object == null) return "";
		return isList(field) ? serializeTable((List) object) : serializeItem(object);
	}

	private String serializeTable(List list) {
		StringBuilder result = new StringBuilder();
		for (Object item : list)
			result.append(serializeItem(item));
		return result.toString();
	}

	private String serializeItem(Object value) {
		Class<?> aClass = value.getClass();
		return "\n" + (isPrimitive(aClass) || isArrayOfPrimitives(aClass) ? value.toString() : new Serializer(value, type(), mapping).toInl());
	}

	private boolean isAttribute(Field field) {
		Class<?> aClass = field.getType();
		return isPrimitive(aClass) || isArrayOfPrimitives(aClass) || isListOfPrimitives(field);
	}

	private boolean isArrayOfPrimitives(Class<?> aClass) {
		return aClass.isArray() && isPrimitive(aClass.getComponentType());
	}

	private boolean isListOfPrimitives(Field field) {
		return field.getType().isAssignableFrom(List.class) && isPrimitive(field.getGenericType().toString());
	}

	private boolean isPrimitive(Class<?> aClass) {
		return isPrimitive(aClass.getName()) || aClass.isPrimitive();
	}

	private static String[] primitives = {"java.lang", "java.util.Date", "java.time"};
	private boolean isPrimitive(String className) {
		if (className.contains("<")) className = className.substring(className.indexOf('<')+1);
		for (String primitive : primitives) if (className.startsWith(primitive)) return true;
		return false;
	}

	private boolean isEmpty(Field field) {
		Object value = valueOf(field);
		return value != null && value.getClass().isArray() && ((Object[]) value).length == 0;
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
