package io.intino.ness.datahubterminalplugin.master;


import io.intino.datahub.model.*;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.magritte.framework.Concept;
import io.intino.magritte.framework.Node;
import io.intino.ness.datahubterminalplugin.Formatters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.intino.itrules.formatters.StringFormatters.firstUpperCase;
import static io.intino.ness.datahubterminalplugin.Formatters.firstUpperCase;
import static io.intino.ness.datahubterminalplugin.Formatters.javaValidName;

public class EntityFrameFactory {
	private static final String DOT = ".";

	static final Map<String, String> TheTypes = Map.of(
			"String", "String",
			"Double", "double",
			"Integer", "int",
			"Long", "long",
			"Boolean", "boolean",
			"Date", "LocalDate",
			"DateTime", "LocalDateTime",
			"Instant", "Instant"
	);

	static final Map<String, String> ListTypes = Map.of(
			"String", "List<String>",
			"Double", "List<Double>",
			"Integer", "List<Integer>",
			"Boolean", "List<Boolean>",
			"Long", "List<Long>",
			"Date", "List<LocalDate>",
			"DateTime", "List<LocalDateTime>",
			"Instant", "List<Instant>"
	);

	static final Map<String, String> SetTypes = Map.of(
			"String", "Set<String>",
			"Double", "Set<Double>",
			"Integer", "Set<Integer>",
			"Boolean", "Set<Boolean>",
			"Long", "Set<Long>",
			"Date", "Set<LocalDate>",
			"DateTime", "Set<LocalDateTime>",
			"Instant", "Set<Instant>"
	);

	private final String workingPackage;
	private final Datamart datamart;

	public EntityFrameFactory(String workingPackage, Datamart datamart) {
		this.workingPackage = workingPackage;
		this.datamart = datamart;
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
				.add("datamart", datamart.name$())
				.add("name", entity.core$().name())
				.add("attribute", entity.core$().componentList().stream().filter(a -> a.is(EntityData.class)).map(a -> attrFrameOf(a, entity.core$())).toArray())
				.add("expression", entity.core$().componentList().stream().filter(a -> a.is(Expression.class)).map(ExpressionHelper::exprFrameOf).toArray());
		if (!datamart.structList().isEmpty()) builder.add("hasStructs", new FrameBuilder().add("package", workingPackage));
		final Parameter parent = parameter(entity.core$(), "entity");
		builder.add("parent", parent != null ? withFullPackage(((Entity) parent.values().get(0)).name$()) : baseEntityName());
		builder.add("normalizeId", new FrameBuilder("normalizeId", (entity.isAbstract() || entity.isDecorable()) ? "abstract" : "").add("package", workingPackage).add("name", entity.name$()).toFrame());
		builder.add("isAbstract", entity.isAbstract() ? "abstract" : "");
		return builder;
	}

	private String withFullPackage(String parent) {
		return workingPackage + ".entities." + parent;
	}

	private String baseEntityName() {
		return workingPackage + "." + firstUpperCase(datamart.name$()) + "Entity";
	}

	private Frame attrFrameOf(Node node, Node owner) {
		FrameBuilder builder = new FrameBuilder().add("attribute");
		node.conceptList().forEach(aspect -> builder.add(aspect.name()));

		EntityData attr = node.as(EntityData.class);
		String type = typeOf(attr);

		if(owner.is(Entity.Abstract.class) || owner.is(Entity.Decorable.class))
			builder.add("castToSubclass", "(" + owner.name() + ")");

		if(attr.isList() || attr.isSet()) {
			String collectionType = attr.isList() ? "List" : "Set";
			builder.add("type", collectionType + "<" + type + ">");
			builder.add("typeParameter", type);
			builder.add("collectionType", collectionType);
		} else if(attr.isMap()) {
			builder.add("type", "Map<String, String>");
			builder.add("typeParameter", "java.lang.String");
			builder.add("collectionType", "Map");
		} else {
			builder.add("type", type);
		}

		builder.add("name", node.name())
				.add("owner", node.owner().name())
				.add("package", workingPackage)
				.add("index", node.owner().componentList().indexOf(node))
				.add("entityOwner", owner.name());

		processParameters(node, builder, type);

		return builder.toFrame();
	}

	private void processParameters(Node node, FrameBuilder builder, String type) {
		Parameter values = parameter(node, "values");
		if (values != null) builder.add("value", values.values().stream().map(Object::toString).toArray());

		Parameter defaultValue = DefaultValueHelper.getDefaultValue(node);
		if (defaultValue != null) builder.add("defaultValue", defaultValue(node, type, defaultValue));
		else builder.add("defaultValue", "null");

		Parameter format = parameter(node, "format");
		if (format != null) builder.add("format", format.values().get(0));
		else if (type.startsWith("Date")) builder.add("format", defaultFormat(type));

		Parameter entity = parameter(node, "entity");
		if (entity != null) {
			String name = ((Entity) entity.values().get(0)).name$(); // TODO: check
			builder.add("entity", name);
			Entity reference = datamart.entityList().stream().filter(e -> e.name$().equals(name)).findFirst().orElse(null);
			if (reference != null && reference.core$().conceptList().stream().anyMatch(c -> c.name().equals("Component"))) {
				builder.add("component");
			}
		}

		Parameter struct = parameter(node, "struct");
		if (struct != null) {
			Struct structNode = ((Struct) struct.values().get(0));
			builder.add("struct", structFrame(structNode)).add("structLength", String.valueOf(structNode.attributeList().size()));
		}

		builder.add("attribute", builder.toFrame());
	}

	private String defaultFormat(String type) { // TODO: try get from master model
		return type.equals("Date") ? "dd/MM/yyyy" : "dd/MM/yyyy HH:mm:ss";
	}

	private Frame structFrame(Struct node) {
		return new FrameBuilder("struct")
				.add("name", node.core$().name())
				.add("package", workingPackage)
				.add("attribute", node.attributeList().stream().map(node1 -> attrFrameOf(node1.core$(), node.core$())).toArray())
				.toFrame();
	}

	private Frame defaultValue(Node c, String type, Parameter defaultValue) {
		FrameBuilder builder = new FrameBuilder(c.conceptList().stream().map(Concept::name).toArray(String[]::new));
		return builder
				.add("type", type)
				.add("package", workingPackage)
				.add("value", defaultValueOf(type, defaultValue))
				.toFrame();
	}

	private static String defaultValueOf(String type, Parameter defaultValue) {
		if(type.contains("List<")) return "null";
		if(type.contains("Set<")) return "null";
		if(type.contains("Map<")) return "null";
		return defaultValue.values().get(0).toString();
	}

	public static String typeParameterOf(String type) {
		return type.substring(type.indexOf("<") + 1, type.lastIndexOf(">"));
	}

	private String typeOfWithCollections(EntityData node) {
		if(node.isList()) return "List";
		if(node.isSet()) return "Set";
		if(node.isMap()) return "Map";
		return typeOf(node);
	}

	private String typeOf(EntityData node) {
		if(node.isDouble()) return "Double";
		if(node.isInteger()) return "Integer";
		if(node.isLong()) return "Long";
		if(node.isBoolean()) return "Boolean";
		if(node.isString()) return "String";
		if(node.isDate()) return "LocalDate";
		if(node.isDateTime()) return "LocalDateTime";
		if(node.isInstant()) return "Instant";
		if(node.isWord()) return node.asWord().name$();
		if(node.isStruct()) return node.asStruct().struct().name$();
		if(node.isEntity()) return node.asEntity().entity().name$();
		throw new RuntimeException("Unknown type of " + node.name$());
	}

	public static boolean isProperTypeName(String s) {
		return !s.equals("Set") && !s.equals("List") && !s.equals("Optional") && !s.equals("Type") && !s.equals("Required");
	}

	private Parameter parameter(Node c, String name) {
		List<?> values = c.variables().get(name);
		return values == null ? null : Parameter.of(values);
	}

	private String calculateEntityPath(Entity entity, String thePackage) {
		return thePackage + DOT + "entities" + DOT
				+ (entity.isDecorable() ? "Abstract" : "")
				+ firstUpperCase().format(javaValidName().format(entity.core$().name()).toString());
	}

	private String calculateDecorableEntityPath(Node node, String aPackage) {
		return aPackage + DOT + "entities" + DOT + firstUpperCase().format(javaValidName().format(node.name()).toString());
	}
}
