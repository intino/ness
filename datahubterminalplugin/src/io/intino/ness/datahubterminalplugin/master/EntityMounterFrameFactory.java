package io.intino.ness.datahubterminalplugin.master;


import io.intino.datahub.model.Datamart;
import io.intino.datahub.model.Entity;
import io.intino.datahub.model.Struct;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.magritte.framework.Concept;
import io.intino.magritte.framework.Node;
import io.intino.ness.datahubterminalplugin.Formatters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.intino.itrules.formatters.StringFormatters.firstUpperCase;
import static io.intino.ness.datahubterminalplugin.Formatters.javaValidName;

public class EntityMounterFrameFactory implements ConceptRenderer {
	private static final String DOT = ".";

	private final String destinationPackage;
	private final String ontologyPackage;
	private final Datamart datamart;

	public EntityMounterFrameFactory(String destinationPackage, String ontologyPackage, Datamart datamart) {
		this.destinationPackage = destinationPackage;
		this.ontologyPackage = ontologyPackage;
		this.datamart = datamart;
	}

	public Map<String, Frame> create(Entity entity) {
		if(entity.isAbstract()) return new HashMap<>(0);
		return Map.of(getMounterPath(entity, destinationPackage), frameOf(entity).toFrame());
	}

	private FrameBuilder frameOf(Entity entity) {
		FrameBuilder builder = new FrameBuilder("mounter")
				.add("message")
				.add("package", destinationPackage)
				.add("ontologypackage", ontologyPackage)
				.add("datamart", datamart.name$())
				.add("name", entity.core$().name())
				.add("attribute", attributesOf(entity).stream().map(this::attrFrameOf).toArray(FrameBuilder[]::new));
		if (!datamart.structList().isEmpty()) builder.add("hasStructs", new FrameBuilder().add("package", destinationPackage));
		final Parameter parent = parameter(entity.core$(), "entity");
		builder.add("parent", parent != null ? ((Entity) parent.values().get(0)).name$() : "io.intino.ness.master.model.Entity");
		builder.add("normalizeId", new FrameBuilder("normalizeId", (entity.isAbstract() || entity.isDecorable()) ? "abstract" : "").add("package", destinationPackage).add("name", entity.name$()).toFrame());
		if (entity.isDecorable() || entity.isAbstract()) builder.add("isAbstract", "abstract");
		if (entity.isDecorable()) builder.add("abstract", "abstract");
		return builder;
	}

	private FrameBuilder attrFrameOf(ConceptAttribute attr) {
		FrameBuilder builder = new FrameBuilder().add("attribute");
		attr.conceptList().forEach(aspect -> builder.add(aspect.name()));

		Node owner = attr.owner();

		if(owner.is(Entity.Abstract.class) || owner.is(Entity.Decorable.class))
			builder.add("castToSubclass", "(" + owner.name() + ")");

		String type = attr.type();
		builder.add("typename", type);
		if(attr.isEntity()) type = entitiesPackage() + type;
		else if(attr.isStruct()) type = structsPackage() + type;
		else if(attr.isWord()) type = entitiesPackage() + attr.owner().name() + "." + Formatters.firstUpperCase(type);

		handleCollectionType(attr, builder, type);

		builder.add("name", attr.name$())
				.add("owner", owner.name())
				.add("package", ontologyPackage)
				.add("index", owner.componentList().indexOf(attr.core$()))
				.add("entityOwner", owner.name());

		processParameters(attr.core$(), builder, type);

		return builder;
	}

	private String entitiesPackage() {
		return ontologyPackage + ".entities.";
	}

	private String structsPackage() {
		return ontologyPackage + ".structs.";
	}

	private static void handleCollectionType(ConceptAttribute attr, FrameBuilder builder, String type) {
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
				.add("package", destinationPackage)
				.add("attribute", attributesOf(struct).stream().map(this::attrFrameOf).toArray())
				.toFrame();
	}

	private Frame defaultValue(Node c, String type, Parameter defaultValue) {
		FrameBuilder builder = new FrameBuilder(c.conceptList().stream().map(Concept::name).toArray(String[]::new));
		return builder
				.add("type", type)
				.add("package", destinationPackage)
				.add("value", defaultValueOf(type, defaultValue))
				.toFrame();
	}

	private static String defaultValueOf(String type, Parameter defaultValue) {
		if(type.contains("List<")) return "null";
		if(type.contains("Set<")) return "null";
		if(type.contains("Map<")) return "null";
		return defaultValue.values().get(0).toString();
	}

	private Parameter parameter(Node c, String name) {
		List<?> values = c.variables().get(name);
		return values == null ? null : Parameter.of(values);
	}

	private String getMounterPath(Entity entity, String aPackage) {
		return aPackage + DOT + "mounters" + DOT + firstUpperCase().format(javaValidName().format(entity.core$().name() + "Mounter").toString());
	}
}
