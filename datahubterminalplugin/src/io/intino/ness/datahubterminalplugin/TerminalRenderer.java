package io.intino.ness.datahubterminalplugin;

import io.intino.datahub.graph.Datalake;
import io.intino.datahub.graph.Terminal;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.itrules.Template;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

class TerminalRenderer {
	private final Terminal terminal;
	private final File srcDir;
	private final String basePackage;

	TerminalRenderer(Terminal terminal, File srcDir, String basePackage) {
		this.terminal = terminal;
		this.srcDir = srcDir;
		this.basePackage = basePackage;
	}

	void render() {
		final File packageFolder = new File(srcDir, basePackage.replace(".", File.separator));
		Commons.writeFrame(packageFolder, terminal.name$(), template().render(createTerminalFrame()));
	}

	private Frame createTerminalFrame() {
		FrameBuilder builder = new FrameBuilder().add("terminal").add("package", basePackage).add("name", terminal.name$());
		if (terminal.publish() != null)
			for (Datalake.Tank.Event tank : terminal.publish().tanks())
				frameOf(tank).forEach(f -> builder.add("publish", f));
		if (terminal.subscribe() != null)
			for (Datalake.Tank.Event tank : terminal.subscribe().tanks())
				frameOf(tank).forEach(f -> builder.add("subscribe", f));
		return builder.toFrame();
	}

	private List<Frame> frameOf(Datalake.Tank.Event tank) {
		List<Frame> frames = new ArrayList<>();
		if (tank.asTank().isContextual()) {
			FrameBuilder builder = new FrameBuilder("context").
					add("type", tank.message().name$()).
					add("context", tank.asTank().asContextual().context().name$()).add("channel", tank.qn());
			Datalake.Context context = tank.asTank().asContextual().context();
			if (!context.isLeaf())
				for (Datalake.Context leaf : context.leafs())
					frames.add(new FrameBuilder("context").
							add("type", tank.message().name$()).
							add("context", leaf.name$()).
							add("channel", (leaf.qn().isEmpty() ? "" : leaf.qn() + ".") + tank.message().name$()).toFrame());
			frames.add(builder.toFrame());
		} else {
			frames.add(new FrameBuilder().
					add("type", tank.message().name$()).
					add("channel", tank.qn()).toFrame());
		}
		return frames;
	}

	private Template template() {
		return Formatters.customize(new TerminalTemplate()).add("typeFormat", (value) -> {
			if (value.toString().contains(".")) return Formatters.firstLowerCase(value.toString());
			else return value;
		});
	}
}
