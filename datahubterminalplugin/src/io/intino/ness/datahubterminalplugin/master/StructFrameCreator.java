package io.intino.ness.datahubterminalplugin.master;


import io.intino.datahub.model.Struct;
import io.intino.datahub.model.Struct.Attribute;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.magritte.framework.Predicate;
import io.intino.magritte.lang.model.Parameter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.intino.itrules.formatters.StringFormatters.firstUpperCase;
import static io.intino.ness.datahubterminalplugin.Formatters.javaValidName;

public class StructFrameCreator {
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
			"Boolean", "List<Boolean",
			"Entity", "List<io.intino.master.model.Entity>",
			"Long", "List<Long>"
	);
	private final String workingPackage;

	public StructFrameCreator(String workingPackage) {
		this.workingPackage = workingPackage;
	}

	public Map<String, Frame> create(Struct struct) {
		Map<String, Frame> map = new HashMap<>(4);
		map.put(calculateStructPath(struct, workingPackage), frameOf(struct).toFrame());
		return map;
	}

	private FrameBuilder frameOf(Struct struct) {
		return new FrameBuilder("struct", "class")
				.add("package", workingPackage)
				.add("name", struct.name$())
				.add("attribute", struct.attributeList().stream().map(this::attrFrameOf).toArray());
	}

	private Frame attrFrameOf(Attribute attr) {
		FrameBuilder builder = new FrameBuilder().add("attribute");
		builder.add(attr.asType().type());
		if (attr.isList()) builder.add("list");
		String type = type(attr);
		builder.add("name", attr.name$()).add("owner", attr.core$().owner().name()).add("type", type);
		List<String> values = attr.isWord() ? attr.asWord().values() : null;
		if (values != null) builder.add("value", values.toArray());
		Parameter defaultValue = null;// parameter(attr, "defaultValue");//TODO
		if (defaultValue != null) builder.add("defaultValue", defaultValue(attr, type, defaultValue));
		String entity = attr.isEntity() ? attr.asEntity().entity().name$() : null;
		if (entity != null) builder.add("entity", entity);
		return builder.toFrame();
	}

	private Frame defaultValue(Attribute attr, String type, Parameter defaultValue) {
		FrameBuilder builder = new FrameBuilder(attr.core$().conceptList().stream().map(Predicate::name).toArray(String[]::new));
		return builder
				.add("type", type)
				.add("value", defaultValue.values().get(0).toString())
				.toFrame();
	}

	private String type(Attribute attribute) {
		String type = attribute.asType().type();
		if (!attribute.isList()) return types.getOrDefault(type, firstUpperCase().format(attribute.name$()).toString());
		return listTypes.getOrDefault(type, "List<" + firstUpperCase().format(attribute.name$()).toString() + ">");

	}

	private String calculateStructPath(Struct struct, String aPackage) {
		return aPackage + DOT + "structs" + DOT + firstUpperCase().format(javaValidName().format(struct.name$()).toString());
	}
}
