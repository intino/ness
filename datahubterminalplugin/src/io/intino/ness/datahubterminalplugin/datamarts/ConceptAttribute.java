package io.intino.ness.datahubterminalplugin.datamarts;

import io.intino.datahub.model.EntityData;
import io.intino.datahub.model.Struct;
import io.intino.magritte.framework.Concept;
import io.intino.magritte.framework.Layer;
import io.intino.magritte.framework.Node;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

import static io.intino.ness.datahubterminalplugin.Formatters.firstUpperCase;

@SuppressWarnings("unchecked")
public class ConceptAttribute {

	private final Object attribute;
	private final Node owner;
	private boolean inherited;
	private String ownerFullName;

	public ConceptAttribute(Object attribute, Node owner) {
		this.attribute = attribute;
		this.owner = owner;
		this.ownerFullName = owner.name();
	}

	public String ownerFullName() {
		return ownerFullName;
	}

	public ConceptAttribute ownerFullName(String ownerFullName) {
		this.ownerFullName = ownerFullName;
		return this;
	}

	public boolean inherited() {
		return inherited;
	}

	public ConceptAttribute inherited(boolean inherited) {
		this.inherited = inherited;
		return this;
	}

	public <T extends Node> T owner() {
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
//		return getOrDefault("isStruct", false);
		return attribute instanceof Struct;
	}

	public boolean isEntity() {
		return getOrDefault("isEntity", false);
	}

	public boolean isMap() {
		return getOrDefault("isMap", false);
	}

	public <T extends Layer> T asWord() {
		return getOrDefault("asWord", null);
	}

	public Struct asStruct() {
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
		if(isWord()) return firstUpperCase(asWord().name$());
		if(isStruct()) return firstUpperCase(asStruct().name$());
		if(isEntity()) return firstUpperCase(asEntity().entity().name$());
		if(isMap()) return "Map";
		throw new RuntimeException("Unknown type of " + name$());
	}

	public Node core$() {
		Node node = getOrDefault("core$", null);
		return node != null ? node : (Node) attribute;
	}

	public boolean shouldAddPackageBeforeName() {
		return true;
	}

	private <T> T getOrDefault(String name, T defValue) {
		try {
			Method method = attribute.getClass().getMethod(name);
			return (T) method.invoke(attribute);
		} catch (Exception e) {
			return defValue;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ConceptAttribute that = (ConceptAttribute) o;
		return inherited == that.inherited && Objects.equals(attribute, that.attribute) && Objects.equals(owner, that.owner);
	}

	@Override
	public int hashCode() {
		return Objects.hash(attribute, owner, inherited);
	}

	public String toString() {
		return name$();
	}
}
