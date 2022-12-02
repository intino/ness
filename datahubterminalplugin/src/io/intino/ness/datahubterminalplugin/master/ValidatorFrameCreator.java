package io.intino.ness.datahubterminalplugin.master;


import io.intino.alexandria.logger.Logger;
import io.intino.datahub.model.Entity;
import io.intino.datahub.model.EntityData;
import io.intino.datahub.model.Struct;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.magritte.framework.Concept;
import io.intino.magritte.framework.Layer;
import io.intino.magritte.framework.Node;

import java.util.*;
import java.util.stream.Stream;

import static io.intino.itrules.formatters.StringFormatters.firstUpperCase;
import static io.intino.ness.datahubterminalplugin.Formatters.javaValidName;

public class ValidatorFrameCreator {
	private static final String DOT = ".";

	private static final Map<String, String> TheTypes = new HashMap<>() {{
		put("String", "String");
		put("Double", "double");
		put("Integer", "integer");
		put("Boolean", "boolean");
		put("Entity", "io.intino.ness.master.model.Entity");
		put("Long", "long");
		put("Date", "LocalDate");
		put("DateTime", "LocalDateTime");
		put("Instant", "Instant");
		put("Map", "Map");
	}};

	private static final Map<String, String> ListTypes = new HashMap<>() {{
		put("String", "List<String>");
		put("Double", "List<Double>");
		put("Integer", "List<Integer>");
		put("Boolean", "List<Boolean");
		put("Entity", "List<io.intino.ness.master.model.Entity>");
		put("Long", "List<Long>");
		put("Date", "List<LocalDate>");
		put("DateTime", "List<LocalDateTime>");
		put("Instant", "List<Instant>");
		put("Map", "List<Map>");
	}};

	private static final Map<String, String> SetTypes = new HashMap<>() {{
		put("String", "Set<String>");
		put("Double", "Set<Double>");
		put("Integer", "Set<Integer>");
		put("Boolean", "Set<Boolean");
		put("Entity", "Set<io.intino.ness.master.model.Entity>");
		put("Long", "Set<Long>");
		put("Date", "Set<LocalDate>");
		put("DateTime", "Set<LocalDateTime>");
		put("Instant", "Set<Instant>");
		put("Map", "Set<Map>");
	}};

	private final String workingPackage;
	private final Set<String> processedTypes = new HashSet<>();

	public ValidatorFrameCreator(String workingPackage) {
		this.workingPackage = workingPackage;
	}

	public Map<String, Frame> create(Entity entity) {
//		if(entity.isAbstract()) return Collections.emptyMap();
		Map<String, Frame> map = new HashMap<>(4);
		map.put(calculateValidatorPath(entity, workingPackage), frameOf(entity).toFrame());
		if (entity.isDecorable()) map.put(calculateDecorableValidatorPath(entity, workingPackage), frameOf(entity).add("decorable").toFrame());
		return map;
	}

	private FrameBuilder frameOf(Entity entity) {
		processedTypes.clear();

		FrameBuilder builder = new FrameBuilder("validator", "class")
				.add("package", workingPackage + DOT + "validators")
				.add("name", entity.core$().name())
				.add("attribute", entity.core$().componentList().stream().filter(c -> c.is(EntityData.class)).map(this::attrFrameOf).toArray())
				.add("type", entity.core$().componentList().stream()
						.filter(c -> c.is(EntityData.class))
						.flatMap(c -> typeFramesOf(c, entity.core$())).filter(Objects::nonNull).toArray());

		final Parameter parent = parameter(entity.core$(), "entity");
		builder.add("parent", parent != null ? ((Entity) parent.values().get(0)).name$() : "io.intino.ness.master.model.Entity");

		if (entity.isDecorable() || entity.isAbstract()) builder.add("isAbstract", "abstract");
		if (entity.isDecorable()) builder.add("abstract", "abstract");

		return builder;
	}

	private Stream<Frame> typeFramesOf(Node node, Node parent) {
		String type = type(node);
		String typename = typename(type, node);
		String typeParameter = typeParameterOf(type);
		return typeFramesOf(node, parent, type, typename, typeParameter);
	}

	private Stream<Frame> typeFramesOf(Node node, Node parent, String type, String typename, String typeParameter) {
		if(!processedTypes.add(type.toLowerCase())) return Stream.empty();
		if(type.startsWith("List<") && typeParameter.equals("String")) return Stream.empty();
		if(type.startsWith("Set<") && typeParameter.equals("String")) return Stream.empty();

		List<Frame> frames = new LinkedList<>();

		FrameBuilder builder = new FrameBuilder("type", typename)
				.add("typename", typename)
				.add("name", typename)
				.add("typeParameter", typeParameter)
				.add("nameBoxed", boxed(type));

		if(node != null) processTypeExtraInfo(node, parent, typeParameter, frames, builder);

		frames.add(builder.toFrame());

		return frames.stream();
	}

	private void processTypeExtraInfo(Node node, Node parent, String typeParameter, List<Frame> frames, FrameBuilder builder) {
		if(!typeParameter.isEmpty() && !typeParameter.equals("Entity") && !typeParameter.equals("String")) {
			typeFramesOf(node, parent,
					typeParameter,
					typeParameter,
					"")
					.forEach(frames::add);
		} else {
			if (node.conceptList().stream().anyMatch(a -> a.name().equals("Word")))
				builder.add("word").add("package", workingPackage + ".entities." + parent.name());

			Parameter struct = parameter(node, "struct");
			if (struct != null)
				builder.add("struct").add("struct", structFrame(((Struct) struct.values().get(0))));
		}
	}

	private String typeParameterOf(String type) {
		if(!type.contains("List<") && !type.contains("Set<")) return "";
		return type.substring(type.indexOf("<") + 1).replace("io.intino.ness.master.model.", "").replace(">", "");
	}

	private String typename(String type, Node node) {
		if (!type.contains("<")) return type;
		return type.substring(0, type.indexOf('<'));
	}

	private String boxed(String type) {
		return type.equals("int") ? "integer" : type;
	}

	private Frame attrFrameOf(Node node) {
		FrameBuilder builder = new FrameBuilder().add("attribute");
		node.conceptList().forEach(aspect -> builder.add(aspect.name()));

		String type = type(node);
		builder.add("name", node.name()).add("owner", node.owner().name()).add("type", type).add("package", workingPackage);
		builder.add("index", node.owner().componentList().indexOf(node));

		builder.add("typename", typename(type, node));
		builder.add("typeParameter", typeParameterOf(type));

		boolean optional = node.conceptList().stream().anyMatch(a -> a.name().equals("Optional"));
		boolean required = node.conceptList().stream().anyMatch(a -> a.name().equals("Required"));

		if(required && optional) {
			Logger.warn("Illegal combination of aspects: " + node.name() + " is declared both as Optional and Required. It will be set to Required.");
			// Illegal combination of aspects. Let's set optional=false because it is more restrictive that way
			optional = false;
		}

		if (optional)
			builder.add("check", new FrameBuilder("optional", "warning").add("name", node.name()).toFrame());

		Parameter values = parameter(node, "values");
		if (values != null) builder.add("value", values.values().stream().map(Object::toString).toArray());

		Parameter defaultValue = DefaultValueHelper.getDefaultValue(node);
		if (defaultValue != null) {
			builder.add("defaultValue", defaultValue(node, type, defaultValue));
			if (!optional) builder.add("optional", new FrameBuilder("optional").add("name", node.name()).toFrame());
		} else if (!optional)
			builder.add("check", new FrameBuilder("required").add("name", node.name()).toFrame());

		Parameter format = parameter(node, "format");
		if (format != null) builder.add("format", format.values().get(0));
		else if (type.contains("Date")) builder.add("format", defaultFormat(type));

		Parameter entity = parameter(node, "entity");
		if (entity != null) builder.add("entity", ((Entity) entity.values().get(0)).name$()); // TODO: check

		Parameter struct = parameter(node, "struct");
		if (struct != null) builder.add("struct", structFrame(((Struct) struct.values().get(0))));

		addMissingAttributeCheckFrame(node, builder, required, optional);

		return builder.toFrame();
	}

	private void addMissingAttributeCheckFrame(Node node, FrameBuilder builder, boolean required, boolean optional) {
		FrameBuilder frame;
		if(required) {
			frame = new FrameBuilder("check", "required");
		} else if(optional) {
			frame = new FrameBuilder("check", "optional", "warning");
		} else {
			frame = new FrameBuilder("check");
		}
		builder.add("check", frame.add("name", node.name()).toFrame());
	}

	private String defaultFormat(String type) { // TODO: try get from master model
		return type.endsWith("LocalDateTime") ? "dd/MM/yyyy HH:mm:ss" : "dd/MM/yyyy";
	}

	private Frame structFrame(Struct node) {
		return new FrameBuilder("struct")
				.add("name", node.core$().name())
				.add("package", workingPackage)
				.add("attribute", node.attributeList().stream().map(Layer::core$).map(this::attrFrameOf).toArray())
				.toFrame();
	}

	private Frame defaultValue(Node c, String type, Parameter defaultValue) {
		FrameBuilder builder = new FrameBuilder(c.conceptList().stream().map(Concept::name).toArray(String[]::new));
		return builder
				.add("type", type)
				.add("value", String.valueOf(defaultValue.values().get(0))) // TODO: check
				.toFrame();
	}

	private String type(Node node) {
		String aspect = node.conceptList().stream().map(Concept::name).filter(this::isProperTypeName).findFirst().orElse("");

		boolean list = node.conceptList().stream().anyMatch(a -> a.name().equals("List"));
		if (list) return ListTypes.getOrDefault(aspect, "List<" + firstUpperCase().format(node.name()).toString() + ">");

		boolean set = node.conceptList().stream().anyMatch(a -> a.name().equals("Set"));
		if (set) return SetTypes.getOrDefault(aspect, "Set<" + firstUpperCase().format(node.name()).toString() + ">");

		return TheTypes.getOrDefault(aspect, firstUpperCase().format(node.name()).toString());
	}

	private boolean isProperTypeName(String s) {
		return !s.equals("Set") && !s.equals("List") && !s.equals("Optional") && !s.equals("Type") && !s.equals("Required");
	}

	private Parameter parameter(Node c, String name) {
		List<?> values = c.variables().get(name);
		return values == null ? null : Parameter.of(values);
	}

	private String calculateValidatorPath(Entity entity, String aPackage) {
		return aPackage + DOT + "validators" + DOT
				+ (entity.isDecorable() ? "Abstract" : "")
				+ firstUpperCase()
				.format(javaValidName().format(entity.name$() + "Validator").toString());
	}

	private String calculateDecorableValidatorPath(Entity node, String aPackage) {
		return aPackage + DOT + "validators" + DOT + firstUpperCase()
				.format(javaValidName().format(node.name$() + "Validator").toString());
	}

}
