package io.intino.ness.datahubterminalplugin.master;


import io.intino.datahub.model.Datamart;
import io.intino.datahub.model.Struct;
import io.intino.datahub.model.StructData;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.magritte.framework.Layer;
import io.intino.magritte.framework.Node;
import io.intino.magritte.framework.Predicate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.intino.itrules.formatters.StringFormatters.firstUpperCase;
import static io.intino.ness.datahubterminalplugin.Formatters.javaValidName;

public class StructFrameFactory implements ConceptRenderer {
	private static final String DOT = ".";

	private final Datamart datamart;
	private final String workingPackage;

	public StructFrameFactory(Datamart datamart, String workingPackage) {
		this.datamart = datamart;
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
				.add("datamart", datamart.name$())
				.add("parent", workingPackage + "." + firstUpperCase().format(datamart.name$()) + "Struct")
				.add("attribute", attributesOf(struct).stream().map(this::attrFrameOf).toArray())
				.add("expression", struct.methodList().stream().map(Layer::core$).map(ExpressionHelper::exprFrameOf).toArray());
	}

		private Frame attrFrameOf(ConceptAttribute attr) {
		FrameBuilder builder = new FrameBuilder().add("attribute");
		String type = attr.type();
		builder.add(type);

		builder.add("name", attr.name$()).add("owner", attr.core$().owner().name()).add("type", type);

		if(attr.isWord()) {
			StructData.Word word = attr.asWord();
			List<String> values = word.values();
			if (values != null) builder.add("value", values.toArray());
		}

		Parameter defaultValue = DefaultValueHelper.getDefaultValue(attr.core$());
		if (defaultValue != null) builder.add("defaultValue", defaultValue(attr.core$(), type, defaultValue));

		String entity = attr.isEntity() ? attr.asEntity().entity().name$() : null;
		if (entity != null) builder.add("entity", entity);
		return builder.toFrame();
	}

	private Frame defaultValue(Node attr, String type, Parameter defaultValue) {
		FrameBuilder builder = new FrameBuilder(attr.conceptList().stream().map(Predicate::name).toArray(String[]::new));
		return builder
				.add("type", type)
				.add("value", defaultValue.values().get(0).toString())
				.toFrame();
	}

	private String calculateStructPath(Struct struct, String thePackage) {
		return thePackage + DOT + "structs" + DOT + firstUpperCase().format(javaValidName().format(struct.name$()).toString());
	}
}
