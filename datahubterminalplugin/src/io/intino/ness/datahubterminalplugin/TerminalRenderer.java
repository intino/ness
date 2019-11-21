package io.intino.ness.datahubterminalplugin;

import io.intino.datahub.graph.Datalake;
import io.intino.datahub.graph.Datalake.Context;
import io.intino.datahub.graph.Message;
import io.intino.datahub.graph.Terminal;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.itrules.Template;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

class TerminalRenderer {
	private final Terminal terminal;
	private final Map<Message, Context> messagesWithContext;
	private final File srcDir;
	private final String basePackage;

	TerminalRenderer(Terminal terminal, Map<Message, Context> messagesWithContext, File srcDir, String basePackage) {
		this.terminal = terminal;
		this.messagesWithContext = messagesWithContext;
		this.srcDir = srcDir;
		this.basePackage = basePackage;
	}

	void render() {
		final File packageFolder = new File(srcDir, basePackage.replace(".", File.separator));
		Commons.writeFrame(packageFolder, terminal.name$(), template().render(createTerminalFrame()));
	}

	private Frame createTerminalFrame() {
		FrameBuilder builder = new FrameBuilder().add("terminal").add("package", basePackage).add("name", terminal.name$());
		builder.add("message", messagesWithContext.keySet().stream().map(m -> new FrameBuilder("message").add("name", m.name$()).add("type", m.name$()).toFrame()).toArray(Frame[]::new));
		if (terminal.publish() != null)
			for (Datalake.Tank.Event tank : terminal.publish().tanks())
				builder.add("publish", frameOf(tank));
		if (terminal.subscribe() != null)
			for (Datalake.Tank.Event tank : terminal.subscribe().tanks())
				builder.add("subscribe", frameOf(tank));
		if (terminal.allowsBpmIn() != null) {
			Context context = terminal.allowsBpmIn().context();
			String statusQn = terminal.allowsBpmIn().processStatusClass();
			String statusClassName = statusQn.substring(statusQn.lastIndexOf(".") + 1);
			Frame frame = new FrameBuilder("default").add("type", statusQn).add("typeName", statusClassName).add("channel", (context != null ? context.qn() + "." : "") + statusClassName).toFrame();
			builder.add("subscribe", frame);
			builder.add("publish", frame);
			builder.add("message", new FrameBuilder("message").add("name", statusClassName).add("type", statusQn).toFrame());
		}

		return builder.toFrame();
	}

	private Frame frameOf(Datalake.Tank.Event tank) {
		return new FrameBuilder(contextsOf(tank).size() > 1 ? "multicontext" : "default").
				add("type", tank.message().name$()).
				add("typeName", tank.message().name$()).
				add("channel", tank.qn()).toFrame();
	}

	private List<Context> contextsOf(Datalake.Tank.Event tank) {
		return tank.asTank().isContextual() ? tank.asTank().asContextual().context().leafs() : Collections.emptyList();
	}

	private Template template() {
		return Formatters.customize(new TerminalTemplate()).add("typeFormat", (value) -> {
			if (value.toString().contains(".")) return Formatters.firstLowerCase(value.toString());
			else return value;
		});
	}
}
