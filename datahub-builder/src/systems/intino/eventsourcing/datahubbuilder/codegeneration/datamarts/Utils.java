package systems.intino.eventsourcing.datahubbuilder.codegeneration.datamarts;

import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import systems.intino.eventsourcing.datahub.model.Attribute;
import systems.intino.eventsourcing.datahub.model.Component;
import systems.intino.eventsourcing.datahub.model.Message;
import systems.intino.eventsourcing.datahub.model.Subject;

import java.util.List;
import java.util.stream.Collectors;

public class Utils {
	public static Frame[] attributeFramesOf(Subject subject) {
		return framesOf(subject.includeList());
	}

	public static List<Subject> subjectsUsing(Message m) {
		return m.graph().datamartList().stream()
				.flatMap(d -> d.subjectList().stream())
				.filter(s -> s.from().stream().anyMatch(t -> t.message().equals(m))).collect(Collectors.toList());

	}

	public static Frame[] framesOf(List<? extends Subject.Include> attributes) {
		return attributes.stream()
				.filter(a -> !a.isStruct())
				.map(Utils::frameOf)
				.toArray(Frame[]::new);
	}

	private static Frame frameOf(Subject.Include include) {
		FrameBuilder fb = frameOf(include.attribute());
		fb.add(include.isReference() ? "Subject" : include.attribute().asType().getClass().getSimpleName().toLowerCase());
		if (include.isReference()) fb.add("targetType", include.asReference().subject().name$());
		return fb.toFrame();
	}

	public static Frame[] attributeFramesOf(List<Attribute> attributes) {
		return attributes.stream()
				.map(Utils::frameOf)
				.map(FrameBuilder::toFrame)
				.toArray(Frame[]::new);

	}

	private static FrameBuilder frameOf(Attribute attribute) {
		FrameBuilder builder = new FrameBuilder("attribute")
				.add("name", attribute.name$());
		if (attribute.core$().owner().is(Component.class))
			builder.add("isStruct", "true").add(attribute.asType().getClass().getSimpleName().toLowerCase());
		if (attribute.asType().type() != null) builder.add("type", attribute.asType().type());
		if (attribute.isList()) builder.add("multiple");
		if (attribute.isWord()) builder.add("words", attribute.asWord().values().toArray());
		if (attribute.core$().owner().is(Component.class)) {
			String path = attribute.core$().owner().fullName();
			String withoutFirst = path.substring(path.indexOf("$") + 1);
			builder.add("prefix", withoutFirst.substring(withoutFirst.indexOf("$") + 1));
		}
		return builder;
	}
}