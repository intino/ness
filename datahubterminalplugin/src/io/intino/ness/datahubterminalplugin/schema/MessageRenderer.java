package io.intino.ness.datahubterminalplugin.schema;

import io.intino.datahub.graph.Data;
import io.intino.datahub.graph.Datalake.Context;
import io.intino.datahub.graph.Message;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.itrules.Template;
import io.intino.ness.datahubterminalplugin.Commons;
import io.intino.ness.datahubterminalplugin.Formatters;

import java.io.File;
import java.util.*;

import static java.util.Collections.addAll;

public class MessageRenderer {
	private final Message message;
	private final Context context;
	private final File destination;
	private final String packageName;

	public MessageRenderer(Message message, Context context, File destination, String packageName) {
		this.message = message;
		this.context = context;
		this.destination = destination;
		this.packageName = packageName;
	}

	public void render() {
		String rootPackage = packageName + ".schemas";
		final File packageFolder = new File(destination, rootPackage.replace(".", File.separator));
		final Frame frame = createMessageFrame(message, rootPackage);
		Commons.writeFrame(packageFolder, message.name$(), template().render(new FrameBuilder("root").add("root", rootPackage).add("package", rootPackage).add("schema", frame)));
	}

	private Frame createMessageFrame(Message schema, String packageName) {
		FrameBuilder messageFrame = createMessageFrame(schema, packageName, new HashSet<>());
		if (context != null) {
			List<Context> leafs = context.isLeaf() ? Collections.singletonList(context) : context.leafs();
			messageFrame.add("context", new FrameBuilder().add("context").add("enum", enums(leafs)));
		}
		return messageFrame.toFrame();
	}

	private Frame[] enums(List<Context> leafs) {
		List<Frame> frames = new ArrayList<>();
		for (Context leaf : leafs) {
			FrameBuilder builder = new FrameBuilder("enum").add("value", leaf.qn());
			if (!leaf.isRoot()) builder.add("parent", leaf.core$().ownerAs(Context.class).qn());
			frames.add(builder.toFrame());
		}
		return frames.toArray(new Frame[0]);
	}

	private FrameBuilder createMessageFrame(Message schema, String packageName, Set<Message> processed) {
		FrameBuilder builder = new FrameBuilder("schema").add("name", schema.name$()).add("package", packageName);
		if (schema.core$().owner().is(Message.class)) builder.add("inner", "static");
		builder.add("attribute", collectAttributes(schema));
		if (schema.isExtensionOf()) builder.add("parent", schema.asExtensionOf().parent().name$());
		final Frame[] components = components(schema, packageName, processed);
		if (components.length > 0) builder.add("schema", components);
		return builder;
	}

	private Frame[] components(Message schema, String packageName, Set<Message> processed) {
		return schema.messageList().stream().filter(processed::add).map(s -> createMessageFrame(s, packageName, processed).toFrame()).toArray(Frame[]::new);
	}

	private FrameBuilder[] collectAttributes(Message message) {
		List<FrameBuilder> attributes = new ArrayList<>();
		addAll(attributes, processAttributes(message.attributeList()));
		addAll(attributes, processComponents(message.messageList()));
		attributes.forEach(f -> f.add("element", message.name$()));
		return attributes.toArray(new FrameBuilder[0]);
	}

	private FrameBuilder[] processAttributes(List<Message.Attribute> attributes) {
		return attributes.stream().map(this::process).toArray(FrameBuilder[]::new);
	}

	private FrameBuilder[] processComponents(List<Message> schemas) {
		return schemas.stream().map(schema -> processMessageAsAttribute(schema, schema.name$(), schema.multiple())).toArray(FrameBuilder[]::new);
	}

	private FrameBuilder process(Message.Attribute attribute) {
		if (attribute.isReal()) return process(attribute.asReal());
		else if (attribute.isInteger()) return process(attribute.asInteger());
		else if (attribute.isBool()) return process(attribute.asBool());
		else if (attribute.isText()) return process(attribute.asText());
		else if (attribute.isDateTime()) return process(attribute.asDateTime());
		else if (attribute.isDate()) return process(attribute.asDate());
		else if (attribute.isFile()) return process(attribute.asFile());
		else if (attribute.isLongInteger()) return process(attribute.asLongInteger());
		else if (attribute.isWord()) return process(attribute.asWord());
		else if (attribute.isObject())
			return processMessageAsAttribute(attribute.asObject().message(), attribute.name$(), attribute.isList());
		return null;
	}

	private FrameBuilder process(Data.Real attribute) {
		return new FrameBuilder("primitive", multiple(attribute) ? "multiple" : "single", "double")
				.add("name", attribute.a$(Message.Attribute.class).name$())
				.add("type", !multiple(attribute) ? "double" : attribute.type())
				.add("defaultValue", attribute.defaultValue());
	}

	private FrameBuilder process(Data.Integer attribute) {
		return new FrameBuilder("primitive", multiple(attribute) ? "multiple" : "single", attribute.type())
				.add("name", attribute.a$(Message.Attribute.class).name$())
				.add("type", !multiple(attribute) ? "int" : attribute.type())
				.add("defaultValue", attribute.defaultValue());
	}

	private FrameBuilder process(Data.LongInteger attribute) {
		return new FrameBuilder("primitive", multiple(attribute) ? "multiple" : "single", attribute.type())
				.add("name", attribute.a$(Message.Attribute.class).name$())
				.add("type", attribute.type())
				.add("defaultValue", attribute.defaultValue() + "L");
	}

	private FrameBuilder process(Data.File attribute) {
		return new FrameBuilder("primitive", multiple(attribute) ? "multiple" : "single", attribute.type())
				.add("name", attribute.a$(Message.Attribute.class).name$())
				.add("type", attribute.type());
	}

	private FrameBuilder process(Data.Bool attribute) {
		return new FrameBuilder("primitive", multiple(attribute) ? "multiple" : "single", attribute.type())
				.add("name", attribute.a$(Message.Attribute.class).name$())
				.add("type", attribute.type())
				.add("defaultValue", attribute.defaultValue());
	}

	private FrameBuilder process(Data.Text attribute) {
		return new FrameBuilder("primitive", multiple(attribute) ? "multiple" : "single", attribute.type())
				.add("name", attribute.a$(Message.Attribute.class).name$())
				.add("type", attribute.type())
				.add("defaultValue", attribute.defaultValue() + "");

	}

	private FrameBuilder process(Data.DateTime attribute) {
		return new FrameBuilder("primitive", multiple(attribute) ? "multiple" : "single", attribute.type())
				.add("name", attribute.a$(Message.Attribute.class).name$())
				.add("type", attribute.type()).add("defaultValue", "null");
	}

	private FrameBuilder process(Data.Date attribute) {
		return new FrameBuilder("primitive", multiple(attribute) ? "multiple" : "single", attribute.type())
				.add("name", attribute.a$(Message.Attribute.class).name$())
				.add("type", attribute.type());
	}

	private FrameBuilder process(Data.Word attribute) {
		final Message.Attribute a = attribute.a$(Message.Attribute.class);
		return new FrameBuilder("primitive", "word", multiple(attribute) ? "multiple" : "single", attribute.type())
				.add("name", a.name$())
				.add("words", attribute.values().toArray(new String[0]))
				.add("type", a.name$());
	}

	private FrameBuilder processMessageAsAttribute(Message schema, String name, boolean multiple) {
		return new FrameBuilder(multiple ? "multiple" : "single", "member", schema.name$())
				.add("name", name)
				.add("type", schema.name$())
				.add("package", thePackage());
	}

	private String thePackage() {
		return packageName + ".schemas";
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
