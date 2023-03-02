package io.intino.ness.datahubterminalplugin.terminal;

import io.intino.datahub.model.Datalake;
import io.intino.datahub.model.Datalake.Tank;
import io.intino.datahub.model.Message;
import io.intino.datahub.model.Namespace;
import io.intino.datahub.model.Terminal;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.itrules.Template;
import io.intino.ness.datahubterminalplugin.Commons;
import io.intino.ness.datahubterminalplugin.Formatters;

import java.io.File;
import java.util.List;

import static io.intino.ness.datahubterminalplugin.Formatters.firstUpperCase;
import static io.intino.ness.datahubterminalplugin.Formatters.snakeCaseToCamelCase;

class TerminalRenderer {
	private final Terminal terminal;
	private final List<Message> messages;
	private final File srcDir;
	private final String rootPackage;
	private final String entitiesPackage;

	TerminalRenderer(Terminal terminal, List<Message> messages, File srcDir, String rootPackage, String entitiesPackage) {
		this.terminal = terminal;
		this.messages = messages;
		this.srcDir = srcDir;
		this.rootPackage = rootPackage;
		this.entitiesPackage = entitiesPackage;
	}

	void render() {
		final File packageFolder = new File(srcDir, rootPackage.replace(".", File.separator));
		Commons.writeFrame(packageFolder, snakeCaseToCamelCase().format(terminal.name$()).toString(), template().render(createTerminalFrame()));
	}

	private Frame createTerminalFrame() {
		Datalake datalake = terminal.graph().datalake();
		FrameBuilder builder = new FrameBuilder("terminal").add("package", rootPackage).add("name", terminal.name$());
		if (datalake != null) builder.add("datalake", "").add("scale", datalake.scale().name());
		builder.add("message", messageFrames());
		if (terminal.publish() != null)
			terminal.publish().messageTanks().forEach(tank -> builder.add("publish", frameOf(tank)));
		if (terminal.subscribe() != null)
			terminal.subscribe().messageTanks().forEach(tank -> builder.add("subscribe", frameOf(tank)));
		if (terminal.bpm() != null) addBpm(builder);
//		if(!terminal.graph().entityList().isEmpty()) builder.add("entities", new FrameBuilder("entities").add("package", rootPackage));
		return builder.toFrame();
	}

	private Frame[] messageFrames() {
		return messages.stream()
				.map(e -> new FrameBuilder("message")
						.add("namespace", messageNamespace(e))
						.add("namespaceQn", messageNamespace(e).replace(".", ""))
						.add("name", e.name$())
						.add("type", messagePackage(e) + "." + firstUpperCase(e.name$())).toFrame())
				.toArray(Frame[]::new);
	}

	private Frame createDatalakeFrame() {
		FrameBuilder datalake = new FrameBuilder().add("datalake").add("package", rootPackage);
		terminal.graph().datalake().tankList().forEach(t -> datalake.add("tank", tank(t)));
		return datalake.toFrame();
	}

	private FrameBuilder tank(Tank t) {
		return new FrameBuilder("tank").add("name", t.name$()).add("qn", t.asMessage().qn());
	}

	private void addBpm(FrameBuilder builder) {
		String statusQn = terminal.bpm().processStatusClass();
		String processStatusQName = statusQn.substring(statusQn.lastIndexOf(".") + 1);
		FrameBuilder bpmBuilder = new FrameBuilder("default", "bpm").
				add("type", statusQn).
				add("typeName", processStatusQName);
		bpmBuilder.add("channel", processStatusQName);
		builder.add("subscribe", bpmBuilder);
		builder.add("publish", bpmBuilder);
		builder.add("event", new FrameBuilder("event").add("name", processStatusQName).add("type", statusQn).toFrame());
	}

	private Frame frameOf(Tank.Message messageTank) {
		String messagesPackage = messagePackage(messageTank.message());
		String namespace = messageNamespace(messageTank.message());
		return new FrameBuilder("default").
				add("type", messagesPackage + "." + firstUpperCase(messageTank.message().name$())).
				add("message", messageTank.message().name$()).
				add("typeName", messageTank.name$()).
				add("namespace", namespace).
				add("namespaceQn", namespace.replace(".", "")).
				add("typeWithNamespace", (namespace.isEmpty() ? "" : namespace + ".") + firstUpperCase(messageTank.name$())).
				add("channel", messageTank.qn()).toFrame();
	}

	private String messagePackage(Message event) {
		String messagePackage = messagesPackage();
		if (event.core$().owner().is(Namespace.class)) messagePackage = messagePackage + "." + messageNamespace(event);
		return messagePackage;
	}

	private String messageNamespace(Message event) {
		return event.core$().owner().is(Namespace.class) ? event.core$().ownerAs(Namespace.class).qn().toLowerCase() : "";
	}

	private String messagesPackage() {
		return rootPackage + ".messages";
	}


	private Template template() {
		return Formatters.customize(new TerminalTemplate()).add("typeFormat", (value) -> {
			if (value.toString().contains(".")) return Formatters.firstLowerCase(value.toString());
			else return value;
		});
	}
}
