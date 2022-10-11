package io.intino.ness.datahubterminalplugin.master;


import io.intino.datahub.model.Entity;
import io.intino.datahub.model.NessGraph;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.magritte.lang.model.Aspect;
import io.intino.magritte.lang.model.Node;
import io.intino.magritte.lang.model.Parameter;
import io.intino.magritte.lang.model.Tag;

import java.util.HashMap;
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
			"Entity", "io.intino.master.model.Entity",
			"Long", "long"
	);

	private static final Map<String, String> listTypes = Map.of(
			"String", "List<String>",
			"Double", "List<Double>",
			"Integer", "List<Integer>",
			"Boolean", "List<Boolean>",
			"Entity", "List<io.intino.master.model.Entity>",
			"Long", "List<Long>"
	);
	private final String workingPackage;
	private final NessGraph model;

	public EntityFrameCreator(String workingPackage, NessGraph model) {
		this.workingPackage = workingPackage;
		this.model = model;
	}

	public Map<String, Frame> create(Entity node) {
		Map<String, Frame> map = new HashMap<>(4);
		map.put(calculateEntityPath(node, workingPackage), frameOf(node).toFrame());
		if (node.is(Tag.Decorable))
			map.put(calculateDecorableEntityPath(node, workingPackage), frameOf(node).add("decorable").toFrame());
		return map;
	}

	private FrameBuilder frameOf(Node node) {
		FrameBuilder builder = new FrameBuilder("entity", "class")
				.add("package", workingPackage)
				.add("name", node.name())
				.add("attribute", node.components().stream().map(this::attrFrameOf).toArray());
		final Parameter parent = parameter(node, "entity");
		builder.add("parent", parent != null ? ((Node) parent.values().get(0)).name() : "io.intino.master.model.Entity");
		if (node.is(Tag.Decorable) || node.isAbstract()) builder.add("isAbstract", "abstract");
		if (node.is(Tag.Decorable)) builder.add("abstract", "abstract");
		return builder;
	}

	private Frame attrFrameOf(Node node) {
		FrameBuilder builder = new FrameBuilder().add("attribute");
		node.appliedAspects().forEach(aspect -> builder.add(aspect.type()));
		String type = type(node);
		builder.add("name", node.name()).add("owner", node.container().name()).add("type", type).add("package", workingPackage);
		builder.add("index", node.container().components().indexOf(node));
		processParameters(node, builder, type);
		return builder.toFrame();
	}

	private void processParameters(Node node, FrameBuilder builder, String type) {
		Parameter values = parameter(node, "values");
		if (values != null) builder.add("value", values.values().stream().map(Object::toString).toArray());
		Parameter defaultValue = parameter(node, "defaultValue");
		if (defaultValue != null) builder.add("defaultValue", defaultValue(node, type, defaultValue));
		Parameter format = parameter(node, "format");
		if (format != null) builder.add("format", format.values().get(0));
		else if (type.startsWith("Date")) builder.add("format", defaultFormat(type));
		Parameter entity = parameter(node, "entity");
		if (entity != null) {
			String name = ((Node) entity.values().get(0)).name();
			builder.add("entity", name);
			Entity reference = model.entityList().stream().filter(e -> e.name$().equals(name)).findFirst().orElse(null);
			if (reference != null && reference.flags().stream().anyMatch(t -> t.name().equals("Component"))) {
				builder.add("component");
			}
		}
		Parameter struct = parameter(node, "struct");
		if (struct != null) builder.add("struct", structFrame(((Node) struct.values().get(0))));
		builder.add("attribute", builder.toFrame());
	}

	private String defaultFormat(String type) { // TODO: try get from master model
		return type.equals("Date") ? "dd/MM/yyyy" : "dd/MM/yyyy HH:mm:ss";
	}

	private Frame structFrame(Node node) {
		return new FrameBuilder("struct")
				.add("name", node.name())
				.add("package", workingPackage)
				.add("attribute", node.components().stream().map(this::attrFrameOf).toArray())
				.toFrame();
	}

	private Frame defaultValue(Node c, String type, Parameter defaultValue) {
		FrameBuilder builder = new FrameBuilder(c.appliedAspects().stream().map(Aspect::type).toArray(String[]::new));
		return builder
				.add("type", type)
				.add("value", defaultValue.values().get(0).toString())
				.toFrame();
	}

	private String type(Node node) {
		String aspect = node.appliedAspects().stream().map(Aspect::type).filter(a -> !a.equals("List")).findFirst().orElse("");
		boolean list = node.appliedAspects().stream().anyMatch(a -> a.type().equals("List"));
		if (!list) return types.getOrDefault(aspect, firstUpperCase().format(node.name()).toString());
		return listTypes.getOrDefault(aspect, "List<" + firstUpperCase().format(node.name()).toString() + ">");

	}

	private Parameter parameter(Node c, String name) {
		return c.parameters().stream().filter(p -> p.name().equals(name)).findFirst().orElse(null);
	}

	private String calculateEntityPath(Node node, String aPackage) {
		return aPackage + DOT + "entities" + DOT + (node.is(Tag.Decorable) ? "Abstract" : "") + firstUpperCase().format(javaValidName().format(node.name()).toString());
	}

	private String calculateDecorableEntityPath(Node node, String aPackage) {
		return aPackage + DOT + "entities" + DOT + firstUpperCase().format(javaValidName().format(node.name()).toString());
	}
}
