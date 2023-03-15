package io.intino.ness.datahubterminalplugin.master;


import io.intino.datahub.model.*;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.magritte.framework.Concept;
import io.intino.magritte.framework.Node;

import java.util.*;

import static io.intino.itrules.formatters.StringFormatters.firstUpperCase;
import static io.intino.ness.datahubterminalplugin.Formatters.firstUpperCase;
import static io.intino.ness.datahubterminalplugin.Formatters.javaValidName;

public class EntityFrameFactory implements ConceptRenderer {
	private static final String DOT = ".";

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
				.add("attribute", attributesOf(entity).stream().map(this::attrFrameOf).toArray(FrameBuilder[]::new))
				.add("expression", entity.methodList().stream().map(m -> ExpressionHelper.exprFrameOf(m, workingPackage)).toArray(Frame[]::new));
		if (!datamart.structList().isEmpty()) builder.add("hasStructs", new FrameBuilder().add("package", workingPackage));
		final Parameter parent = parameter(entity.core$(), "entity");
		builder.add("parent", parent != null ? withFullPackage(((Entity) parent.values().get(0)).name$()) : baseEntityName());
		builder.add("normalizeId", new FrameBuilder("normalizeId", (entity.isAbstract() || entity.isDecorable()) ? "abstract" : "").add("package", workingPackage).add("name", entity.name$()).toFrame());
		builder.add("isAbstract", entity.isAbstract() ? "abstract" : "");
		return builder;
	}

	private String withFullPackage(String parent) {
		return entitiesPackage() + parent;
	}

	private String baseEntityName() {
		return workingPackage + "." + firstUpperCase(datamart.name$()) + "Entity";
	}

	private FrameBuilder attrFrameOf(ConceptAttribute attr) {
		FrameBuilder builder = new FrameBuilder().add("attribute");
		attr.conceptList().forEach(aspect -> builder.add(aspect.name()));

		Node owner = attr.owner();

		if(owner.is(Entity.Abstract.class) || owner.is(Entity.Decorable.class))
			builder.add("castToSubclass", "(" + owner.name() + ")");

		if(attr.inherited())builder.add("inherited");

		String type = attr.type();
		builder.add("typename", type);
		if(attr.isEntity()) type = entitiesPackage() + type;
		else if(attr.isStruct()) type = structsPackage() + type;
		else if(attr.isWord()) type = firstUpperCase(type);

		handleCollectionType(attr, builder, type);

		builder.add("name", attr.name$())
				.add("owner", owner.name())
				.add("package", workingPackage)
				.add("index", owner.componentList().indexOf(attr.core$()))
				.add("entityOwner", owner.name());

		processParameters(attr.core$(), builder, type);

		return builder;
	}

	private void handleCollectionType(ConceptAttribute attr, FrameBuilder builder, String type) {
		if(attr.isList() || attr.isSet()) {
			String collectionType = attr.isList() ? "List" : "Set";
			builder.add("type", collectionType + "<" + type + ">");
			builder.add("collectionType", collectionType);
		} else if(attr.isMap()) {
			builder.add("type", "Map<String, String>");
			builder.add("collectionType", "Map");
		} else {
			builder.add("type", type);
		}
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

	private Frame structFrame(Struct struct) {
		return new FrameBuilder("struct")
				.add("name", struct.core$().name())
				.add("package", workingPackage)
				.add("attribute", attributesOf(struct).stream().map(this::attrFrameOf).toArray())
				.toFrame();
	}

	private String defaultValue(Node c, String type, Parameter defaultValue) {
		return defaultValueOf(type, defaultValue);
//		FrameBuilder builder = new FrameBuilder(c.conceptList().stream().map(Concept::name).toArray(String[]::new)).add("defaultValue");
//		return builder
//				.add("type", type)
//				.add("package", workingPackage)
//				.add("value", defaultValueOf(type, defaultValue))
//				.toFrame();
	}

	private static String defaultValueOf(String type, Parameter defaultValue) {
		if(type.contains("List<")) return "new java.util.ArrayList<>()";
		if(type.contains("Set<")) return "new java.util.HashSet<>()";
		if(type.contains("Map<")) return "new java.util.HashMap<>()";
		return defaultValue.values().get(0).toString();
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

	private String structsPackage() {
		return workingPackage + ".structs.";
	}

	private String entitiesPackage() {
		return workingPackage + ".entities.";
	}
}
