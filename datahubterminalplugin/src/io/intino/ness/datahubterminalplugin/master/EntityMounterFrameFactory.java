package io.intino.ness.datahubterminalplugin.master;


import io.intino.datahub.model.*;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.magritte.framework.Concept;
import io.intino.magritte.framework.Node;

import java.util.*;
import java.util.stream.Collectors;

import static io.intino.itrules.formatters.StringFormatters.firstUpperCase;
import static io.intino.ness.datahubterminalplugin.Formatters.javaValidName;

// TODO: parse attributes from parent entities and implicit attributes from event T.T
public class EntityMounterFrameFactory {
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

	public EntityMounterFrameFactory(String workingPackage, Datamart datamart) {
		this.workingPackage = workingPackage;
		this.datamart = datamart;
	}

	public Map<String, Frame> create(Entity entity) {
		if(entity.isAbstract()) return new HashMap<>(0);
		return Map.of(getMounterPath(entity, workingPackage), frameOf(entity).toFrame());
	}

	private FrameBuilder frameOf(Entity entity) {
		FrameBuilder builder = new FrameBuilder("mounter")
				.add("message")
				.add("package", workingPackage)
				.add("datamart", datamart.name$())
				.add("name", entity.core$().name())
				.add("attribute", attributesOf(entity).toArray(Frame[]::new));
		if (!datamart.structList().isEmpty()) builder.add("hasStructs", new FrameBuilder().add("package", workingPackage));
		final Parameter parent = parameter(entity.core$(), "entity");
		builder.add("parent", parent != null ? ((Entity) parent.values().get(0)).name$() : "io.intino.ness.master.model.Entity");
		builder.add("normalizeId", new FrameBuilder("normalizeId", (entity.isAbstract() || entity.isDecorable()) ? "abstract" : "").add("package", workingPackage).add("name", entity.name$()).toFrame());
		if (entity.isDecorable() || entity.isAbstract()) builder.add("isAbstract", "abstract");
		if (entity.isDecorable()) builder.add("abstract", "abstract");
		return builder;
	}

	private Collection<Frame> attributesOf(Entity entity) {
		Map<String, FrameBuilder> attribs = new LinkedHashMap<>();
		getAttributesFromEvent(entity, entity.from().message().attributeList(), attribs);
		getAttributesFromParents(entity, attribs);
		getAttributesFromEntity(entity.attributeList(), attribs);
		return attribs.values().stream().map(FrameBuilder::toFrame).collect(Collectors.toList());
	}

	private void getAttributesFromEvent(Entity entity, List<Attribute> attributes, Map<String, FrameBuilder> map) {
		for(Attribute attr : attributes) {
			if(entity.exclude().contains(attr.name$())) continue;
			FrameBuilder b = new FrameBuilder("attribute");
			if(attr.isList()) b.add("list");
			map.put(attr.name$(), b.add("name", attr.name$()).add("type", typeOf(attr)));
		}
	}

	private void getAttributesFromParents(Entity entity, Map<String, FrameBuilder> map) {
		if(!entity.isExtensionOf()) return;
		List<Entity.Attribute> attributes = new ArrayList<>();
		Entity parent = entity.asExtensionOf().entity();
		while(parent != null) {
			attributes.addAll(parent.attributeList());
			parent = parent.isExtensionOf() ? parent.asExtensionOf().entity() : null;
		}
		Collections.reverse(attributes);
		getAttributesFromEntity(attributes, map);
	}

	private void getAttributesFromEntity(List<Entity.Attribute> attributes, Map<String, FrameBuilder> map) {
		for(Entity.Attribute attr : attributes) {
			FrameBuilder b = new FrameBuilder("attribute");
			if(attr.isList()) b.add("list");
			if(attr.isSet()) b.add("set");
			if(attr.isMap()) b.add("map");
			if(attr.isEntity()) b.add("entity");
			map.put(attr.name$(), b.add("name", attr.name$()).add("type", typeOf(attr)));
		}
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

	private String typeOf(Attribute node) {
		if(node.isReal()) return "Double";
		if(node.isInteger()) return "Integer";
		if(node.isLongInteger()) return "Long";
		if(node.isBool()) return "Boolean";
		if(node.isText()) return "String";
		if(node.isDate()) return "LocalDate";
		if(node.isDateTime()) return "Instant";
		if(node.isWord()) return node.asWord().name$();
		throw new RuntimeException("Unknown type of " + node.name$());
	}

	private Frame attrFrameOf(Node node, Node owner) {
		FrameBuilder builder = new FrameBuilder().add("attribute");
		node.conceptList().forEach(aspect -> builder.add(aspect.name()));
		String type = typeOf(node);

		builder.add("name", node.name())
				.add("owner", node.owner().name())
				.add("type", type)
				.add("package", workingPackage)
				.add("index", node.owner().componentList().indexOf(node))
				.add("entityOwner", owner.name());

		if(owner.is(Entity.Abstract.class) || owner.is(Entity.Decorable.class))
			builder.add("castToSubclass", "(" + owner.name() + ")");

		if(type.contains("List<") || type.contains("Set<")) {
			builder.add("typeParameter", typeParameterOf(type));
		} // TODO: for maps

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

	public static String typeOf(Node node) {
		String aspect = node.conceptList().stream().map(Concept::name).filter(EntityMounterFrameFactory::isProperTypeName).findFirst().orElse("");

		boolean list = node.conceptList().stream().anyMatch(a -> a.name().equals("List"));
		if (list) return ListTypes.getOrDefault(aspect, "List<" + firstUpperCase().format(node.name()).toString() + ">");

		boolean set = node.conceptList().stream().anyMatch(a -> a.name().equals("Set"));
		if (set) return SetTypes.getOrDefault(aspect, "Set<" + firstUpperCase().format(node.name()).toString() + ">");

		boolean map = node.conceptList().stream().anyMatch(a -> a.name().equals("Map"));
		if (map) return "Map<String, String>";

		return TheTypes.getOrDefault(aspect, firstUpperCase().format(node.name()).toString());
	}

	public static boolean isProperTypeName(String s) {
		return !s.equals("Set") && !s.equals("List") && !s.equals("Optional") && !s.equals("Type") && !s.equals("Required");
	}

	private Parameter parameter(Node c, String name) {
		List<?> values = c.variables().get(name);
		return values == null ? null : Parameter.of(values);
	}

	private String getMounterPath(Entity entity, String aPackage) {
		return aPackage + DOT + "mounters" + DOT + firstUpperCase().format(javaValidName().format(entity.core$().name() + "Mounter").toString());
	}
}
