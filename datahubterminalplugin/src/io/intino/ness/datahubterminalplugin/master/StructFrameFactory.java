package io.intino.ness.datahubterminalplugin.master;


import io.intino.datahub.model.Datamart;
import io.intino.datahub.model.Struct;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.intino.itrules.formatters.StringFormatters.firstUpperCase;
import static io.intino.ness.datahubterminalplugin.Formatters.javaValidName;

public class StructFrameFactory implements ConceptRenderer {
	private static final String DOT = ".";
	public static final String STRUCT_INTERNAL_CLASS_SEP = "$";

	private final Datamart datamart;
	private final String workingPackage;

	public StructFrameFactory(Datamart datamart, String workingPackage) {
		this.datamart = datamart;
		this.workingPackage = workingPackage;
	}

	public Map<String, Frame> create(Struct struct) {
		return create(struct, null);
	}

	public Map<String, Frame> create(Struct struct, String ownerName) {
		Map<String, Frame> map = new HashMap<>(1);
		map.put(calculateStructPath(struct, workingPackage), frameOf(struct, ownerName).toFrame());
		return map;
	}

	private FrameBuilder frameOf(Struct struct, String ownerName) {
		boolean internalClass = ownerName != null;
		String name = ownerName + STRUCT_INTERNAL_CLASS_SEP + struct.name$();

		List<Frame> attributes = attributesOf(struct).stream().map(this::attrFrameOf).map(FrameBuilder::toFrame).collect(Collectors.toList());

		FrameBuilder builder = new FrameBuilder("struct", "class")
				.add("package", workingPackage)
				.add("name", struct.name$())
				.add("definitionname", name)
				.add("datamart", datamart.name$())
				.add("parent", workingPackage + "." + firstUpperCase().format(datamart.name$()) + "Struct")
				.add("expression", struct.methodList().stream().map(e -> ExpressionHelper.exprFrameOf(e, workingPackage)).toArray(Frame[]::new))
				.add("struct", struct.structList().stream().map(s -> frameOf(s, name)).map(FrameBuilder::toFrame).toArray(Frame[]::new));

		if(internalClass) builder.add("static", " static");
		else builder.add("standalone", header());

		struct.structList().stream().map(s -> attrFrameOf(attrOf(struct.core$(), s)).toFrame()).forEach(attributes::add);

		builder.add("attribute", attributes.toArray(Frame[]::new));

		return builder;
	}

	private Frame header() {
		return new FrameBuilder("standalone")
				.add("package", workingPackage)
				.add("datamart", datamart.name$())
				.toFrame();
	}

	@Override
	public Datamart datamart() {
		return datamart;
	}

	@Override
	public String workingPackage() {
		return workingPackage;
	}

	private String calculateStructPath(Struct struct, String thePackage) {
		return thePackage + DOT + "structs" + DOT + firstUpperCase().format(javaValidName().format(struct.name$()).toString());
	}
}
