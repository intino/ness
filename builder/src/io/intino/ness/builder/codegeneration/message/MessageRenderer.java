package io.intino.ness.builder.codegeneration.message;

import io.intino.datahub.model.*;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.itrules.Template;
import io.intino.ness.builder.codegeneration.Commons;
import io.intino.ness.builder.codegeneration.Formatters;

import java.io.File;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.stream;

public class MessageRenderer {
	private static final String EVENT = "io.intino.alexandria.event.message.MessageEvent";
	private final Message message;
	private final File destination;
	private final String rootPackage;

	public MessageRenderer(Message message, File destination, String rootPackage) {
		this.message = message;
		this.destination = destination;
		this.rootPackage = rootPackage;
	}

	public void render() {
		String rootPackage = messagesPackage();
		if (message.core$().owner().is(Namespace.class))
			rootPackage = rootPackage + "." + message.core$().ownerAs(Namespace.class).qn();
		final File packageFolder = new File(destination, rootPackage.replace(".", File.separator));
		final Frame frame = createEventFrame(message, rootPackage);
		Commons.writeFrame(packageFolder, message.name$(), template().render(new FrameBuilder("root").add("root", rootPackage).add("package", rootPackage).add("event", frame)));
	}

	private Frame createEventFrame(Message event, String packageName) {
		FrameBuilder eventFrame = new FrameBuilder("event").
				add("name", event.name$()).add("package", packageName).
				add("parent", parent(event));
		if (isAssertion(event)) {
			eventFrame.add("assertionId", "id")
					.add("assertionInfo", new FrameBuilder("assertion").add("name", event.name$()));
		}
		eventFrame.add("attribute", processAttributesOf(attributesOf(event), event.name$()));
		Map<Component, Boolean> components = collectComponents(event);
		if (!components.isEmpty()) eventFrame.add("component", processComponents(components, event.name$()));
		return eventFrame.toFrame();
	}

	private boolean isAssertion(Message m) {
		return m.isAssertion() || (m.isExtensionOf() && isAssertion(m.asExtensionOf().parent()));
	}

	private String parent(Message message) {
		if (message.isExtensionOf()) {
			Message parent = message.asExtensionOf().parent();
			String eventPackage = messagesPackage();
			if (parent.core$().owner().is(Namespace.class))
				eventPackage = eventPackage + "." + parent.core$().ownerAs(Namespace.class).qn();
			return eventPackage + "." + parent.name$();
		}
		return EVENT;
	}

	private FrameBuilder[] processAttributesOf(Stream<Attribute> attributes, String owner) {
		FrameBuilder[] frames = attributes.map(this::process).toArray(FrameBuilder[]::new);
		stream(frames).forEach(f -> f.add("owner", owner));
		return frames;
	}

	private Stream<Attribute> attributesOf(Message message) {
		return message.attributeList().stream(); // TODO: if message is an assertion, then add id and enable attributes (if not already defined)
	}

	private FrameBuilder[] processComponents(Map<Component, Boolean> components, String owner) {
		return components.entrySet().stream().map(e -> processComponent(e.getKey(), owner, e.getValue())).toArray(FrameBuilder[]::new);
	}

	private FrameBuilder processComponent(Component component, String owner, boolean multiple) {
		FrameBuilder builder = new FrameBuilder("component", multiple ? "multiple" : "single").
				add("name", component.name$()).
				add("type", component.name$()).
				add("owner", owner).
				add("attribute", processAttributesOf(component.attributeList().stream(), component.name$()));
		if (component.isExtensionOf()) builder.add("parent", component.asExtensionOf().parent().name$());
		Map<Component, Boolean> components = collectComponents(component);
		if (!components.isEmpty()) builder.add("component", processComponents(components, component.name$()));
		return builder;
	}

	private Map<Component, Boolean> collectComponents(Message message) {
		Map<Component, Boolean> components = message.componentList().stream().collect(Collectors.toMap(c -> c, Component::multiple));
		components.putAll(message.hasList().stream().collect(Collectors.toMap(Message.Has::element, Message.Has::multiple)));
		return components;
	}

	private Map<Component, Boolean> collectComponents(Component component) {
		Map<Component, Boolean> components = component.componentList().stream().collect(Collectors.toMap(c -> c, Component::multiple));
		components.putAll(component.hasList().stream().collect(Collectors.toMap(Component.Has::element, Component.Has::multiple)));
		return components;
	}

	private FrameBuilder process(Attribute attribute) {
		if (attribute.isReal()) return process(attribute.asReal());
		else if (attribute.isInteger()) return process(attribute.asInteger());
		else if (attribute.isBool()) return process(attribute.asBool());
		else if (attribute.isText()) return process(attribute.asText());
		else if (attribute.isDateTime()) return process(attribute.asDateTime());
		else if (attribute.isDate()) return process(attribute.asDate());
		else if (attribute.isLongInteger()) return process(attribute.asLongInteger());
		else if (attribute.isWord()) return process(attribute.asWord());
		return null;
	}

	private FrameBuilder process(Data.Real attribute) {
		FrameBuilder frameBuilder = new FrameBuilder("primitive", multiple(attribute) ? "multiple" : "single", "double");
		if (message.isRetrocompatible()) frameBuilder.add("retrocompatible");
		return frameBuilder
				.add("name", attribute.a$(Attribute.class).name$())
				.add("type", attribute.type())
				.add("simpleType", attribute.type().substring(attribute.type().lastIndexOf(".") + 1))
				.add("objectType", attribute.type())
				.add("defaultValue", attribute.defaultValue());
	}

	private FrameBuilder process(Data.Integer attribute) {
		FrameBuilder frameBuilder = new FrameBuilder("primitive", multiple(attribute) ? "multiple" : "single", attribute.type(), "int");
		if (message.isRetrocompatible()) frameBuilder.add("retrocompatible");
		return frameBuilder
				.add("name", attribute.a$(Attribute.class).name$())
				.add("type", attribute.type())
				.add("simpleType", attribute.type().substring(attribute.type().lastIndexOf(".") + 1))
				.add("objectType", attribute.type())
				.add("defaultValue", attribute.defaultValue());
	}

	private FrameBuilder process(Data.LongInteger attribute) {
		FrameBuilder frameBuilder = new FrameBuilder("primitive", multiple(attribute) ? "multiple" : "single", attribute.type(), "long");
		if (message.isRetrocompatible()) frameBuilder.add("retrocompatible");
		return frameBuilder
				.add("name", attribute.a$(Attribute.class).name$())
				.add("type", attribute.type())
				.add("simpleType", attribute.type().substring(attribute.type().lastIndexOf(".") + 1))
				.add("defaultValue", attribute.defaultValue() + "L");
	}


	private FrameBuilder process(Data.Bool attribute) {
		FrameBuilder frameBuilder = new FrameBuilder("primitive", multiple(attribute) ? "multiple" : "single", attribute.type(), "bool");
		if (message.isRetrocompatible()) frameBuilder.add("retrocompatible");
		return frameBuilder
				.add("name", attribute.a$(Attribute.class).name$())
				.add("type", attribute.type())
				.add("simpleType", attribute.type())
				.add("defaultValue", attribute.defaultValue());
	}

	private FrameBuilder process(Data.Text attribute) {
		return new FrameBuilder("primitive", multiple(attribute) ? "multiple" : "single", attribute.type(), "text")
				.add("name", attribute.a$(Attribute.class).name$())
				.add("type", attribute.type())
				.add("simpleType", attribute.type())
				.add("defaultValue", attribute.defaultValue() + "");
	}

	private FrameBuilder process(Data.DateTime attribute) {
		return new FrameBuilder("primitive", multiple(attribute) ? "multiple" : "single", attribute.type(), "datetime")
				.add("name", attribute.a$(Attribute.class).name$())
				.add("type", attribute.type())
				.add("simpleType", attribute.type().substring(attribute.type().lastIndexOf(".") + 1))
				.add("defaultValue", "null");
	}

	private FrameBuilder process(Data.Date attribute) {
		return new FrameBuilder("primitive", multiple(attribute) ? "multiple" : "single", attribute.type(), "date")
				.add("name", attribute.a$(Attribute.class).name$())
				.add("simpleType", attribute.type().substring(attribute.type().lastIndexOf(".") + 1))
				.add("type", attribute.type());
	}

	private FrameBuilder process(Data.Word attribute) {
		final Attribute a = attribute.a$(Attribute.class);
		return new FrameBuilder("primitive", "word", multiple(attribute) ? "multiple" : "single", attribute.type())
				.add("name", a.name$())
				.add("words", attribute.values().toArray(new String[0]))
				.add("type", a.name$());
	}

	private String messagesPackage() {
		return rootPackage + ".messages";
	}

	private boolean multiple(Data.Type attribute) {
		return attribute.asData().isList();
	}

	private Template template() {
		return Formatters.customize(new MessageTemplate()).add("typeFormat", (value) -> {
			if (value.toString().contains(".")) return Formatters.firstLowerCase(value.toString());
			else return value;
		});
	}
}
