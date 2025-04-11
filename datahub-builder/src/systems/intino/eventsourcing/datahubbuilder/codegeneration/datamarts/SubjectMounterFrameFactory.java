package systems.intino.eventsourcing.datahubbuilder.codegeneration.datamarts;


import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import systems.intino.eventsourcing.datahub.model.*;
import systems.intino.eventsourcing.datahubbuilder.codegeneration.Formatters;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.intino.itrules.formatters.StringFormatters.firstUpperCase;

public class SubjectMounterFrameFactory {
	private static final String DOT = ".";

	private final String destinationPackage;
	private final String ontologyPackage;
	private final Datamart datamart;

	public SubjectMounterFrameFactory(String destinationPackage, String ontologyPackage, Datamart datamart) {
		this.destinationPackage = destinationPackage;
		this.ontologyPackage = ontologyPackage;
		this.datamart = datamart;
	}

	public Map<String, Frame> create(Subject subject) {
		if (subject.isAbstract()) return new HashMap<>(0);
		return Map.of(getMounterPath(subject, destinationPackage), frameOf(subject).toFrame());
	}

	private FrameBuilder frameOf(Subject subject) {
		String subjectName = firstUpperCase().format(subject.name$()).toString();
		FrameBuilder builder = new FrameBuilder("mounter")
				.add("message")
				.add("package", destinationPackage)
				.add("ontologypackage", ontologyPackage)
				.add("datamart", datamart.name$())
				.add("name", subject.core$().name())
				.add("attribute", Utils.framesOf(subject.includeList()))
				.add("struct", structs(subject).stream().map(s -> structFrameOf(s, subject.name$(), messageComponentOf(subject, s))).toArray(FrameBuilder[]::new))
				.add("normalizeId", new FrameBuilder("normalizeId", (subject.isAbstract() || subject.isDecorable()) ? "abstract" : "").add("package", destinationPackage).add("name", subjectName).toFrame());

		if (subject.isExtensionOf())
			builder.add("parent", subject.asExtensionOf().subject().name$());
		if (subject.isDecorable() || subject.isAbstract()) builder.add("isAbstract", "abstract");
		if (subject.isDecorable()) builder.add("abstract", "abstract");
		return builder;
	}

	private static String messageComponentOf(Subject subject, Component struct) {
		Message message = subject.from().stream().map(Datalake.Tank.Message::message)
				.filter(m -> m.component(c -> c.name$().equals(struct.name$())) != null)
				.findFirst()
				.orElse(null);
		return message == null ? null : message.name$();
	}

	private static List<Component> structs(Subject subject) {
		List<Component> structs = componentsOf(subject);
		Subject parent = subject.isExtensionOf() ? subject.asExtensionOf().subject() : null;
		while (parent != null) {
			structs.addAll(componentsOf(parent));
			parent = parent.isExtensionOf() ? parent.asExtensionOf().subject() : null;
		}
		Collections.reverse(structs);
		return structs.stream().distinct().toList();
	}

	private static List<Component> componentsOf(Subject subject) {
		return subject.includeList(Subject.Include::isStruct).stream().map(i -> i.asStruct().messageComponent()).collect(Collectors.toList());
	}

	private FrameBuilder structFrameOf(Component struct, String ownerName, String messageEvent) {
		FrameBuilder builder = new FrameBuilder("struct");
		if (struct.multiple()) builder.add("multiple");
		String fullName = ownerName + "." + firstUpperCase().format(struct.name$());
		String type = ownerName.contains(".") ? ownerName + "." + firstUpperCase().format(struct.name$()) : messageEvent + "." + firstUpperCase().format(struct.name$());
		builder.add("name", ownerName.replace(".", StructFrameFactory.STRUCT_INTERNAL_CLASS_SEP) + StructFrameFactory.STRUCT_INTERNAL_CLASS_SEP + struct.name$());
		builder.add("attribName", struct.multiple() ? struct.name$() + "List" : struct.name$());
		builder.add("fullName", fullName);
		builder.add("type", type);
		builder.add("typename", firstUpperCase().format(struct.name$()));
		builder.add("package", ontologyPackage + ".structs");
		builder.add("attribute", Utils.attributeFramesOf(struct.attributeList()));
		builder.add("struct", struct.componentList().stream().map(s -> structFrameOf(s, fullName, messageEvent)).toArray(FrameBuilder[]::new));
		return builder;
	}


	private String getMounterPath(Subject subject, String aPackage) {
		return aPackage + DOT + "mounters" + DOT + firstUpperCase().format(Formatters.javaValidName().format(subject.core$().name() + "Mounter").toString());
	}
}