package io.intino.ness.master.data;

public class ComponentAttributeDefinition {

	private final String name;
	private final String component;
	private final Type type;

	public ComponentAttributeDefinition(String name, String component, Type type) {
		this.name = name;
		this.component = component;
		this.type = type;
	}

	public String name() {
		return name;
	}

	public String component() {
		return component;
	}

	public Type type() {
		return type;
	}

	public enum Type {
		Reference, List, Map
	}
}
