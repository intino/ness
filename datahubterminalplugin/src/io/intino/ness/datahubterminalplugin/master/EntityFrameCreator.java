package io.intino.ness.datahubterminalplugin.master;


import io.intino.datahub.model.Entity;
import io.intino.datahub.model.NessGraph;
import io.intino.datahub.model.Struct;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.magritte.framework.Concept;
import io.intino.magritte.framework.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.intino.itrules.formatters.StringFormatters.firstUpperCase;
import static io.intino.ness.datahubterminalplugin.Formatters.javaValidName;

public class EntityFrameCreator {
	private static final String DOT = ".";
	private static final Map<String, String> types = Map.of(
			"String", "String",
			"Double", "double",
			"Integer", "int",
			"Boolean", "boolean",
			"Entity", "io.intino.ness.master.model.Entity",
			"Long", "long"
	);

	private static final Map<String, String> listTypes = Map.of(
			"String", "List<String>",
			"Double", "List<Double>",
			"Integer", "List<Integer>",
			"Boolean", "List<Boolean>",
			"Entity", "List<io.intino.ness.master.model.Entity>",
			"Long", "List<Long>"
	);
	private final String workingPackage;
	private final NessGraph model;

	public EntityFrameCreator(String workingPackage, NessGraph model) {
		this.workingPackage = workingPackage;
		this.model = model;
	}

	public Map<String, Frame> create(Entity entity) {
		Map<String, Frame> map = new HashMap<>(4);
		map.put(calculateEntityPath(entity, workingPackage), frameOf(entity).toFrame());
		if (entity.isDecorable())
			map.put(calculateDecorableEntityPath(entity.core$(), workingPackage), frameOf(entity).add("decorable").toFrame());
		return map;
	}

	private FrameBuilder frameOf(Entity entity) {
		FrameBuilder builder = new FrameBuilder("entity", "class")
				.add("package", workingPackage)
				.add("name", entity.core$().name())
				.add("attribute", entity.core$().componentList().stream().map(a -> attrFrameOf(a, entity.core$())).toArray());
		final Parameter parent = parameter(entity.core$(), "entity");
		builder.add("parent", parent != null ? ((Entity) parent.values().get(0)).name$() : "io.intino.ness.master.model.Entity");
		if (entity.isDecorable() || entity.isAbstract()) builder.add("isAbstract", "abstract");
		if (entity.isDecorable()) builder.add("abstract", "abstract");
		return builder;
	}

	private Frame attrFrameOf(Node node, Node owner) {
		FrameBuilder builder = new FrameBuilder().add("attribute");
		node.conceptList().forEach(aspect -> builder.add(aspect.name()));
		String type = type(node);
		builder.add("name", node.name()).add("owner", node.owner().name()).add("type", type).add("package", workingPackage);
		builder.add("index", node.owner().componentList().indexOf(node));
		builder.add("entityOwner", owner.name());
		if(owner.is(Entity.Abstract.class) || owner.is(Entity.Decorable.class))
			builder.add("castToSubclass", "(" + owner.name() + ")");
		processParameters(node, builder, type);
		return builder.toFrame();
	}

	private void processParameters(Node node, FrameBuilder builder, String type) {
		Parameter values = parameter(node, "values");
		if (values != null) builder.add("value", values.values().stream().map(Object::toString).toArray());

		Parameter defaultValue = DefaultValueHelper.getDefaultValue(node);
		if (defaultValue != null) builder.add("defaultValue", defaultValue(node, type, defaultValue));

		Parameter format = parameter(node, "format");
		if (format != null) builder.add("format", format.values().get(0));
		else if (type.startsWith("Date")) builder.add("format", defaultFormat(type));

		Parameter entity = parameter(node, "entity");
		if (entity != null) {
			String name = ((Entity) entity.values().get(0)).name$(); // TODO: check
			builder.add("entity", name);
			Entity reference = model.entityList().stream().filter(e -> e.name$().equals(name)).findFirst().orElse(null);
			if (reference != null && reference.core$().conceptList().stream().anyMatch(c -> c.name().equals("Component"))) {
				builder.add("component");
			}
		}

		Parameter struct = parameter(node, "struct");
		if (struct != null) builder.add("struct", structFrame(((Struct) struct.values().get(0)).core$()));
		builder.add("attribute", builder.toFrame());
	}

	private String defaultFormat(String type) { // TODO: try get from master model
		return type.equals("Date") ? "dd/MM/yyyy" : "dd/MM/yyyy HH:mm:ss";
	}

	private Frame structFrame(Node node) {
		return new FrameBuilder("struct")
				.add("name", node.name())
				.add("package", workingPackage)
				.add("attribute", node.componentList().stream().map(node1 -> attrFrameOf(node1, node)).toArray())
				.toFrame();
	}

	private Frame defaultValue(Node c, String type, Parameter defaultValue) {
		FrameBuilder builder = new FrameBuilder(c.conceptList().stream().map(Concept::name).toArray(String[]::new));
		return builder
				.add("type", type)
				.add("value", defaultValueOf(type, defaultValue))
				.toFrame();
	}

	private static String defaultValueOf(String type, Parameter defaultValue) {
		if(type.contains("List<")) return "new java.util.ArrayList<>()";
		if(type.contains("Map<")) return "new java.util.HashMap<>()";
		return defaultValue.values().get(0).toString();
	}

	private String type(Node node) {
		String aspect = node.conceptList().stream().map(Concept::name).filter(this::isProperTypeName).findFirst().orElse("");
		boolean list = node.conceptList().stream().anyMatch(a -> a.name().equals("List"));
		if (!list) return types.getOrDefault(aspect, firstUpperCase().format(node.name()).toString());
		return listTypes.getOrDefault(aspect, "List<" + firstUpperCase().format(node.name()).toString() + ">");
	}

	private boolean isProperTypeName(String s) {
		return !s.equals("List") && !s.equals("Optional") && !s.equals("Type");
	}

	private Parameter parameter(Node c, String name) {
		List<?> values = c.variables().get(name);
		return values == null ? null : Parameter.of(values);
	}

	private String calculateEntityPath(Entity entity, String aPackage) {
		return aPackage + DOT + "entities" + DOT
				+ (entity.isDecorable() ? "Abstract" : "")
				+ firstUpperCase().format(javaValidName().format(entity.core$().name()).toString());
	}

	private String calculateDecorableEntityPath(Node node, String aPackage) {
		return aPackage + DOT + "entities" + DOT + firstUpperCase().format(javaValidName().format(node.name()).toString());
	}
}
