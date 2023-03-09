package io.intino.ness.datahubterminalplugin.master;

import io.intino.datahub.model.EntityData;
import io.intino.magritte.framework.Concept;
import io.intino.magritte.framework.Node;

import java.lang.reflect.Method;
import java.util.List;

@SuppressWarnings("unchecked")
public class ConceptAttribute {

	private final Object attribute;
	private final Node owner;

	public ConceptAttribute(Object attribute, Node owner) {
		this.attribute = attribute;
		this.owner = owner;
	}

	public <T> T owner() {
		return (T) owner;
	}

	public String name$() {
		return getOrDefault("name$", getOrDefault("name", null));
	}

	public List<Concept> conceptList() {
		return core$().conceptList();
	}

	public boolean isList() {
		return getOrDefault("isList", false);
	}

	public boolean isSet() {
		return getOrDefault("isSet", false);
	}

	public boolean isReal() {
		return getOrDefault("isReal", false);
	}

	public boolean isLongInteger() {
		return getOrDefault("isLongInteger", false);
	}

	public boolean isBool() {
		return getOrDefault("isBool", false);
	}

	public boolean isText() {
		return getOrDefault("isText", false);
	}

	public boolean isDouble() {
		return getOrDefault("isDouble", false);
	}

	public boolean isInteger() {
		return getOrDefault("isInteger", false);
	}

	public boolean isLong() {
		return getOrDefault("isLong", false);
	}

	public boolean isBoolean() {
		return getOrDefault("isBoolean", false);
	}

	public boolean isString() {
		return getOrDefault("isString", false);
	}

	public boolean isDate() {
		return getOrDefault("isDate", false);
	}

	public boolean isDateTime() {
		return getOrDefault("isDateTime", false);
	}

	public boolean isInstant() {
		return getOrDefault("isInstant", false);
	}

	public boolean isWord() {
		return getOrDefault("isWord", false);
	}

	public boolean isStruct() {
		return getOrDefault("isStruct", false);
	}

	public boolean isEntity() {
		return getOrDefault("isEntity", false);
	}

	public boolean isMap() {
		return getOrDefault("isMap", false);
	}

	public Node asWord() {
		return getOrDefault("asWord", null);
	}

	public EntityData.Struct asStruct() {
		return getOrDefault("asStruct", null);
	}

	public EntityData.Entity asEntity() {
		return getOrDefault("asEntity", null);
	}

	public String type() {
		if(isLongInteger() || isLong()) return "Long";
		if(isBool() || isBoolean()) return "Boolean";
		if(isText() || isString()) return "String";
		if(isDouble() || isReal()) return "Double";
		if(isInteger()) return "Integer";
		if(isDate()) return "LocalDate";
		if(isDateTime()) return "LocalDateTime";
		if(isInstant()) return "Instant";
		if(isWord()) return asWord().name();
		if(isStruct()) return asStruct().struct().name$();
		if(isEntity()) return asEntity().entity().name$();
		if(isMap()) return "Map";
		throw new RuntimeException("Unknown type of " + name$());
	}

	public Node core$() {
		Node node = getOrDefault("core$", null);
		return node != null ? node : (Node) attribute;
	}

	private <T> T getOrDefault(String name, T defValue) {
		try {
			Method method = attribute.getClass().getMethod(name);
			return (T) method.invoke(attribute);
		} catch (Exception e) {
			return defValue;
		}
	}
}
