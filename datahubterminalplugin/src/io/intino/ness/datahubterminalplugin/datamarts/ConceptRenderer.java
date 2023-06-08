package io.intino.ness.datahubterminalplugin.datamarts;

import io.intino.datahub.model.Datamart;
import io.intino.datahub.model.Entity;
import io.intino.datahub.model.Struct;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.magritte.framework.Node;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static io.intino.ness.datahubterminalplugin.Formatters.firstUpperCase;

public interface ConceptRenderer {

	Datamart datamart();
	String workingPackage();

	default String structsPackage() {
		return workingPackage() + ".structs.";
	}

	default String entitiesPackage() {
		return workingPackage() + ".entities.";
	}

	default Parameter parameter(Node c, String name) {
		List<?> values = c.variables().get(name);
		return values == null ? null : Parameter.of(values);
	}

	default List<ConceptAttribute> attributesOf(Struct struct) {
		return attributesOf(struct, ConceptAttribute::new);
	}

	default List<ConceptAttribute> attributesOf(Struct struct, BiFunction<Object, Node, ConceptAttribute> ctor) {
		return struct.attributeList().stream().map(a -> ctor.apply(a, struct.core$())).collect(Collectors.toList());
	}

	default List<ConceptAttribute> attributesOf(Entity entity) {
		return attributesOf(entity, ConceptAttribute::new);
	}

	default List<ConceptAttribute> attributesOf(Entity entity, BiFunction<Object, Node, ConceptAttribute> ctor) {
		Map<String, ConceptAttribute> map = new LinkedHashMap<>();
		Helper.getAttributesFromParents(entity, map, ctor);
		Helper.getAttributesFromEntity(entity, entity.attributeList(), map, false, ctor);
		return new ArrayList<>(map.values());
	}

	default ConceptAttribute attrOf(Node owner, Struct structAttr) {
		return new ConceptAttribute(structAttr, owner) {
			@Override
			public String name$() {
				return isList() ? super.name$() + "List" : super.name$();
			}

			@Override
			public boolean isList() {
				return structAttr.multiple();
			}

			@Override
			public boolean isStruct() {
				return true;
			}

			@Override
			public boolean shouldAddPackageBeforeName() {
				return false;
			}

			@Override
			public Struct asStruct() {
				return structAttr;
			}
		};
	}

	default FrameBuilder attrFrameOf(ConceptAttribute attr) {
		FrameBuilder builder = new FrameBuilder().add("attribute");
		attr.conceptList().forEach(aspect -> builder.add(aspect.name()));

		Node owner = attr.owner();

		if(owner.is(Entity.Abstract.class) || owner.is(Entity.Decorable.class))
			builder.add("castToSubclass", "(" + owner.name() + ")");

		if(attr.inherited()) builder.add("inherited");

		String type = attr.type();
		builder.add("typename", type);

		if(attr.isEntity() && attr.shouldAddPackageBeforeName()) type = entitiesPackage() + type;
		else if(attr.isStruct() && attr.shouldAddPackageBeforeName()) type = structsPackage() + type;
		else if(attr.isWord()) type = firstUpperCase(attr.ownerFullName()) + "." + type;

		handleCollectionType(attr, builder, type);

		builder.add("name", attr.name$())
				.add("owner", owner.name())
				.add("package", workingPackage())
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
			builder.add("typeParameter", type);
		} else if(attr.isMap()) {
			builder.add("type", "Map<String, String>");
			builder.add("collectionType", "Map");
			builder.add("typeParameter", type);
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
			Entity reference = datamart().entityList().stream().filter(e -> e.name$().equals(name)).findFirst().orElse(null);
			if (reference != null && reference.core$().conceptList().stream().anyMatch(c -> c.name().equals("Component"))) {
				builder.add("component");
			}
		}

		Parameter struct = parameter(node, "struct");
		if (struct != null) {
			Struct structNode = ((Struct) struct.values().get(0));
			builder.add("struct", paramStructFrame(structNode)).add("structLength", String.valueOf(structNode.attributeList().size()));
		}

//		builder.add("attribute", builder.toFrame());
	}

	private String defaultFormat(String type) { // TODO: try get from master model
		return type.equals("Date") ? "dd/MM/yyyy" : "dd/MM/yyyy HH:mm:ss";
	}

	private Frame paramStructFrame(Struct struct) {
		return new FrameBuilder("struct")
				.add("name", struct.core$().name())
				.add("package", workingPackage())
				.add("attribute", attributesOf(struct).stream().map(this::attrFrameOf).toArray())
				.toFrame();
	}

	private String defaultValue(Node c, String type, Parameter defaultValue) {
		return "null";
//		return defaultValueOf(type, defaultValue);
	}

	private static String defaultValueOf(String type, Parameter defaultValue) {
		if(type.contains("List<")) return "new java.util.ArrayList<>()";
		if(type.contains("Set<")) return "new java.util.HashSet<>()";
		if(type.contains("Map<")) return "new java.util.HashMap<>()";
		return defaultValue.values().get(0).toString();
	}

	class Helper {

		private static void getAttributesFromParents(Entity entity, Map<String, ConceptAttribute> map, BiFunction<Object, Node, ConceptAttribute> ctor) {
			if(!entity.isExtensionOf()) return;
			List<Entity.Attribute> attributes = new ArrayList<>();
			Entity parent = entity.asExtensionOf().entity();
			while(parent != null) {
				attributes.addAll(parent.attributeList());
				parent = parent.isExtensionOf() ? parent.asExtensionOf().entity() : null;
			}
			Collections.reverse(attributes);
			getAttributesFromEntity(entity, attributes, map, true, ctor);
		}

		private static void getAttributesFromEntity(Entity entity, List<Entity.Attribute> attributeList,
													Map<String, ConceptAttribute> attribs, boolean inherited, BiFunction<Object, Node, ConceptAttribute> ctor) {
			attributeList.forEach(a -> attribs.put(a.name$(), ctor.apply(a, entity.core$()).inherited(inherited)));
		}
	}
}
