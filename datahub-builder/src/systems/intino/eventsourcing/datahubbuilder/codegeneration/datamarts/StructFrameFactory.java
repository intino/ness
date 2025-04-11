package systems.intino.eventsourcing.datahubbuilder.codegeneration.datamarts;


import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import systems.intino.eventsourcing.datahub.model.Component;
import systems.intino.eventsourcing.datahub.model.Datamart;

import java.util.HashMap;
import java.util.Map;

import static io.intino.itrules.formatters.StringFormatters.firstUpperCase;
import static systems.intino.eventsourcing.datahubbuilder.codegeneration.Formatters.javaValidName;
import static systems.intino.eventsourcing.datahubbuilder.codegeneration.datamarts.Utils.attributeFramesOf;

public record StructFrameFactory(Datamart datamart, String workingPackage) {
	private static final String DOT = ".";
	public static final String STRUCT_INTERNAL_CLASS_SEP = "$";

	public Map<String, Frame> create(Component struct) {
		return create(struct, null);
	}

	public Map<String, Frame> create(Component struct, String ownerName) {
		Map<String, Frame> map = new HashMap<>(1);
		map.put(calculateStructPath(struct, workingPackage), frameOf(struct, ownerName).toFrame());
		return map;
	}

	private FrameBuilder frameOf(Component component, String ownerName) {
		boolean internalClass = ownerName != null;
		String name = ownerName + STRUCT_INTERNAL_CLASS_SEP + component.name$();
		FrameBuilder builder = new FrameBuilder("struct", "class")
				.add("package", workingPackage)
				.add("name", component.name$())
				.add("definitionname", name)
				.add("datamart", datamart.name$())
				.add("parent", workingPackage + "." + firstUpperCase().format(datamart.name$()) + "Struct")
//				.add("expression", struct.methodList().stream().map(e -> ExpressionHelper.exprFrameOf(e, workingPackage)).toArray(Frame[]::new))
				.add("struct", component.componentList().stream().map(s -> frameOf(s, name)).map(FrameBuilder::toFrame).toArray(Frame[]::new));
		if (component.multiple()) builder.add("multiple");
		if (internalClass) builder.add("static", " static");
		else builder.add("standalone", header());
		builder.add("attribute", attributeFramesOf(component.attributeList()));
		return builder;
	}

	private Frame header() {
		return new FrameBuilder("standalone")
				.add("package", workingPackage)
				.add("datamart", datamart.name$())
				.toFrame();
	}

	private String calculateStructPath(Component struct, String thePackage) {
		return thePackage + DOT + "structs" + DOT + firstUpperCase().format(javaValidName().format(struct.name$()).toString());
	}
}