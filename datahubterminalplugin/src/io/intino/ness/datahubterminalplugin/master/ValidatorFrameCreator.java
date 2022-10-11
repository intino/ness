package io.intino.ness.datahubterminalplugin.master;


import io.intino.datahub.model.Entity;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.magritte.framework.Concept;
import io.intino.magritte.framework.Node;

import java.util.*;

import static io.intino.itrules.formatters.StringFormatters.firstUpperCase;
import static io.intino.ness.datahubterminalplugin.Formatters.javaValidName;

public class ValidatorFrameCreator {
	private static final String DOT = ".";
	private static final Map<String, String> types = Map.of(
			"String", "String",
			"Double", "double",
			"Integer", "int",
			"Boolean", "boolean",
			"Entity", "io.intino.master.model.Entity",
			"Long", "long",
			"Date", "LocalDate",
			"DateTime", "LocalDateTime",
			"Instant", "Instant",
			"Map", "Map"
	);

	private static final Map<String, String> listTypes = Map.of(
			"String", "List<String>",
			"Double", "List<Double>",
			"Integer", "List<Integer>",
			"Boolean", "List<Boolean",
			"Entity", "List<io.intino.master.model.Entity>",
			"Long", "List<Long>",
			"Date", "List<LocalDate>",
			"DateTime", "List<LocalDateTime>",
			"Instant", "List<Instant>",
			"Map", "List<Map>"
	);
	private final String workingPackage;

	private final Set<String> processedTypes = new HashSet<>();

	public ValidatorFrameCreator(String workingPackage) {
		this.workingPackage = workingPackage;
	}


	public Map<String, Frame> create(Entity node) {
//		if (node.is(Tag.Abstract)) return Collections.emptyMap();
		Map<String, Frame> map = new HashMap<>(4);
		map.put(calculateValidatorPath(node, workingPackage), frameOf(node.core$()).toFrame());
//		if (node.is(Tag.Decorable)) map.put(calculateDecorableValidatorPath(node, workingPackage), frameOf(node).add("decorable").toFrame());
		return map;
	}

	private FrameBuilder frameOf(Node node) {
		processedTypes.clear();
		FrameBuilder builder = new FrameBuilder("validator", "class")
				.add("package", workingPackage + DOT + "validators")
				.add("name", node.name())
				.add("attribute", node.componentList().stream().map(this::attrFrameOf).toArray())
				.add("type", node.componentList().stream().map(c -> typeFrameOf(c, node)).filter(Objects::nonNull).toArray());
		final Parameter parent = parameter(node, "entity");
		builder.add("parent", parent != null ? ((Node) parent.values().get(0)).name() : "io.intino.master.model.Entity");
//		if (node.is(Tag.Decorable) || node.isAbstract()) builder.add("isAbstract", "abstract");
//		if (node.is(Tag.Decorable)) builder.add("abstract", "abstract");
		return builder;
	}

	private Frame typeFrameOf(Node node, Node parent) {
		String type = type(node);
		if (!processedTypes.add(type)) return null;

		FrameBuilder builder = new FrameBuilder("type", type)
				.add("typename", typename(type))
				.add("name", typename(type))
				.add("nameBoxed", boxed(type));

		if (node.conceptList().stream().anyMatch(a -> a.name().equals("Word")))
			builder.add("word").add("package", workingPackage + ".entities." + parent.name());

		Parameter struct = parameter(node, "struct");
		if (struct != null)
			builder.add("struct").add("struct").add("struct", structFrame(((Node) struct.values().get(0))));

		return builder.toFrame();
	}

	private String typename(String type) {
		if (!type.contains("<")) return type;
		return type.substring(0, type.indexOf('<'));
	}

	private String nameBoxed(String type) {
		return type.equals("int") ? "integer" : type;
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

		builder.add("typename", typename(type));

		boolean optional = node.conceptList().stream().anyMatch(a -> a.name().equals("Optional"));
		if (optional)
			builder.add("optional", new FrameBuilder("optional", "warning").add("name", node.name()).toFrame());

		Parameter values = parameter(node, "values");
		if (values != null) builder.add("value", values.values().stream().map(Object::toString).toArray());

		Parameter defaultValue = parameter(node, "defaultValue");
		if (defaultValue != null) {
			builder.add("defaultValue", defaultValue(node, type, defaultValue));
			if (!optional) builder.add("optional", new FrameBuilder("optional").add("name", node.name()).toFrame());
		} else if (!optional)
			builder.add("required", new FrameBuilder("required").add("name", node.name()).toFrame());

		Parameter format = parameter(node, "format");
		if (format != null) builder.add("format", format.values().get(0));
		else if (type.contains("Date")) builder.add("format", defaultFormat(type));

		Parameter entity = parameter(node, "entity");
		if (entity != null) builder.add("entity", ((Node) entity.values().get(0)).name());

		Parameter struct = parameter(node, "struct");
		if (struct != null) builder.add("struct", structFrame(((Node) struct.values().get(0))));

		return builder.toFrame();
	}

	private String defaultFormat(String type) { // TODO: try get from master model
		return type.endsWith("LocalDateTime") ? "dd/MM/yyyy HH:mm:ss" : "dd/MM/yyyy";
	}

	private Frame structFrame(Node node) {
		return new FrameBuilder("struct")
				.add("name", node.name())
				.add("package", workingPackage)
				.add("attribute", node.componentList().stream().map(this::attrFrameOf).toArray())
				.toFrame();
	}

	private Frame defaultValue(Node c, String type, Parameter defaultValue) {
		FrameBuilder builder = new FrameBuilder(c.conceptList().stream().map(Concept::name).toArray(String[]::new));
		return builder
				.add("type", type)
				.add("value", defaultValue.values().get(0).toString())
				.toFrame();
	}

	private String type(Node node) {
		String aspect = node.conceptList().stream().map(Concept::name).filter(a -> !a.equals("List")).findFirst().orElse("");
		boolean list = node.conceptList().stream().anyMatch(a -> a.name().equals("List"));
		if (!list) return types.getOrDefault(aspect, firstUpperCase().format(node.name()).toString());
		return listTypes.getOrDefault(aspect, "List<" + firstUpperCase().format(node.name()).toString() + ">");
	}

	private Parameter parameter(Node c, String name) {
		List<?> values = c.variables().get(name);
		return values == null ? null : Parameter.of(values);
	}

	private String calculateValidatorPath(Entity node, String aPackage) {
		return aPackage + DOT + "validators" + DOT
//				+ (node.is(Tag.Decorable) ? "Abstract" : "")
				+ firstUpperCase()
				.format(javaValidName().format(node.name$() + "Validator").toString());
	}

	private String calculateDecorableValidatorPath(Entity node, String aPackage) {
		return aPackage + DOT + "validators" + DOT + firstUpperCase()
				.format(javaValidName().format(node.name$() + "Validator").toString());
	}

}
