package io.intino.ness.datahubterminalplugin.event;

import io.intino.datahub.graph.*;
import io.intino.datahub.graph.Datalake.Context;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.itrules.Template;
import io.intino.ness.datahubterminalplugin.Commons;
import io.intino.ness.datahubterminalplugin.Formatters;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

public class EventRenderer {
	private static final String EVENT = "io.intino.alexandria.event.Event";
	private final Event event;
	private final Context context;
	private final File destination;
	private final String rootPackage;

	public EventRenderer(Event event, Context context, File destination, String rootPackage) {
		this.event = event;
		this.context = context;
		this.destination = destination;
		this.rootPackage = rootPackage;
	}

	public void render() {
		String rootPackage = eventsPackage();
		if (event.core$().owner().is(Namespace.class))
			rootPackage = rootPackage + "." + event.core$().ownerAs(Namespace.class).name$();
		final File packageFolder = new File(destination, rootPackage.replace(".", File.separator));
		final Frame frame = createEventFrame(event, rootPackage);
		Commons.writeFrame(packageFolder, event.name$(), template().render(new FrameBuilder("root").add("root", rootPackage).add("package", rootPackage).add("event", frame)));
	}

	private Frame createEventFrame(Event event, String packageName) {
		FrameBuilder eventFrame = new FrameBuilder("event").
				add("name", event.name$()).add("package", packageName).
				add("parent", event.isExtensionOf() ? event.asExtensionOf().parent().name$() : EVENT);
		if (event.isExtensionOf()) eventFrame.add("parentSuper", event.name$());
		eventFrame.add("attribute", processAttributes(event.attributeList(), event.name$()));
		Map<Component, Boolean> components = collectComponents(event);
		if (!components.isEmpty()) eventFrame.add("component", processComponents(components, event.name$()));
		if (context != null) {
			List<Context> leafs = context.isLeaf() ? Collections.singletonList(context) : context.leafs();
			eventFrame.add("context", new FrameBuilder().add("context").add("enum", enums(context, leafs)));
		}
		return eventFrame.toFrame();
	}

	private FrameBuilder[] processAttributes(List<Attribute> attributes, String owner) {
		FrameBuilder[] frames = attributes.stream().map(this::process).toArray(FrameBuilder[]::new);
		stream(frames).forEach(f -> f.add("owner", owner));
		return frames;
	}

	private FrameBuilder[] processComponents(Map<Component, Boolean> components, String owner) {
		return components.entrySet().stream().map(e -> processComponent(e.getKey(), owner, e.getValue())).toArray(FrameBuilder[]::new);
	}

	private FrameBuilder processComponent(Component component, String owner, boolean multiple) {
		FrameBuilder builder = new FrameBuilder("component", multiple ? "multiple" : "single").
				add("name", component.name$()).
				add("type", component.name$()).
				add("owner", owner).
				add("attribute", processAttributes(component.attributeList(), component.name$()));
		if (component.isExtensionOf()) builder.add("parent", component.asExtensionOf().parent().name$());
		Map<Component, Boolean> components = collectComponents(component);
		if (!components.isEmpty()) builder.add("component", processComponents(components, component.name$()));
		return builder;
	}

	private Map<Component, Boolean> collectComponents(Event event) {
		Map<Component, Boolean> components = event.componentList().stream().collect(Collectors.toMap(c -> c, Component::multiple));
		components.putAll(event.hasList().stream().collect(Collectors.toMap(Event.Has::element, Event.Has::multiple)));
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
		else if (attribute.isTable()) return process(attribute.asTable());
		return null;
	}

	private Frame[] enums(Context realContext, List<Context> leafs) {
		List<Frame> frames = new ArrayList<>();
		if (!leafs.contains(realContext) && !realContext.label().isEmpty())
			frames.add(new FrameBuilder("enum").add("value", realContext.qn().replace(".", "-")).toFrame());
		for (Context leaf : leafs) {
			FrameBuilder builder = new FrameBuilder("enum").add("value", leaf.qn().replace(".", "-")).add("qn", leaf.qn());
			frames.add(builder.toFrame());
		}
		return frames.toArray(new Frame[0]);
	}

	private FrameBuilder process(Data.Real attribute) {
		return new FrameBuilder("primitive", multiple(attribute) ? "multiple" : "single", "double")
				.add("name", attribute.a$(Attribute.class).name$())
				.add("type", !multiple(attribute) ? "double" : attribute.type())
				.add("simpleType", attribute.type().substring(attribute.type().lastIndexOf(".") + 1))
				.add("objectType", attribute.type())
				.add("defaultValue", attribute.defaultValue());
	}

	private FrameBuilder process(Data.Integer attribute) {
		return new FrameBuilder("primitive", multiple(attribute) ? "multiple" : "single", attribute.type())
				.add("name", attribute.a$(Attribute.class).name$())
				.add("type", !multiple(attribute) ? "int" : attribute.type())
				.add("simpleType", attribute.type().substring(attribute.type().lastIndexOf(".") + 1))
				.add("objectType", attribute.type())
				.add("defaultValue", attribute.defaultValue());
	}

	private FrameBuilder process(Data.LongInteger attribute) {
		return new FrameBuilder("primitive", multiple(attribute) ? "multiple" : "single", attribute.type())
				.add("name", attribute.a$(Attribute.class).name$())
				.add("type", attribute.type())
				.add("simpleType", attribute.type().substring(attribute.type().lastIndexOf(".") + 1))
				.add("defaultValue", attribute.defaultValue() + "L");
	}


	private FrameBuilder process(Data.Bool attribute) {
		return new FrameBuilder("primitive", multiple(attribute) ? "multiple" : "single", attribute.type())
				.add("name", attribute.a$(Attribute.class).name$())
				.add("type", attribute.type())
				.add("simpleType", attribute.type())
				.add("defaultValue", attribute.defaultValue());
	}

	private FrameBuilder process(Data.Text attribute) {
		return new FrameBuilder("primitive", multiple(attribute) ? "multiple" : "single", attribute.type())
				.add("name", attribute.a$(Attribute.class).name$())
				.add("type", attribute.type())
				.add("simpleType", attribute.type())
				.add("defaultValue", attribute.defaultValue() + "");
	}

	private FrameBuilder process(Data.DateTime attribute) {
		return new FrameBuilder("primitive", multiple(attribute) ? "multiple" : "single", attribute.type())
				.add("name", attribute.a$(Attribute.class).name$())
				.add("type", attribute.type())
				.add("simpleType", attribute.type().substring(attribute.type().lastIndexOf(".") + 1))
				.add("defaultValue", "null");
	}

	private FrameBuilder process(Data.Date attribute) {
		return new FrameBuilder("primitive", multiple(attribute) ? "multiple" : "single", attribute.type())
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

	private FrameBuilder process(Data.Table table) {
		final Attribute a = table.a$(Attribute.class);
		return new FrameBuilder("table")
				.add("name", a.name$())
				.add("column", columns(table.columnList()))
				.add("type", "table");
	}

	private Frame[] columns(List<Data.Table.Column> columns) {
		return columns.stream().map(c -> {
			FrameBuilder builder = new FrameBuilder("column", c.asType().getClass().getSimpleName()).
					add("name", c.name$()).
					add("simpleType", simpleType(c)).
					add("type", c.asType().type());
			if (c.isWord()) builder.add("word", c.asWord().values().toArray(String[]::new));
			return builder.toFrame();
		}).toArray(Frame[]::new);
	}

	private String simpleType(Data.Table.Column c) {
		if (c.isWord()) return c.name$();
		String type = c.asType().type();
		return type.contains(".") ? type.substring(type.lastIndexOf(".") + 1) : type;
	}

	private String eventsPackage() {
		return rootPackage + ".events";
	}

	private boolean multiple(Data.Type attribute) {
		return attribute.asData().isList();
	}

	private Template template() {
		return Formatters.customize(new EventTemplate()).add("typeFormat", (value) -> {
			if (value.toString().contains(".")) return Formatters.firstLowerCase(value.toString());
			else return value;
		});
	}

}
