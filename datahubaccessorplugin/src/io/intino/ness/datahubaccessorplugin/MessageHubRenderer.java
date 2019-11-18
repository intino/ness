package io.intino.ness.datahubaccessorplugin;

import io.intino.datahub.graph.Datalake;
import io.intino.datahub.graph.MessageHub;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.itrules.Template;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

class MessageHubRenderer {
	private final MessageHub messageHub;
	private final File srcDir;
	private final String basePackage;

	MessageHubRenderer(MessageHub messageHub, File srcDir, String basePackage) {
		this.messageHub = messageHub;
		this.srcDir = srcDir;
		this.basePackage = basePackage;
	}

	void render() {
		final File packageFolder = new File(srcDir, basePackage.replace(".", File.separator));
		Commons.writeFrame(packageFolder, messageHub.name$(), template().render(createMessageHubFrame()));
	}

	private Frame createMessageHubFrame() {
		FrameBuilder builder = new FrameBuilder().add("accessor").add("package", basePackage).add("name", messageHub.name$());
		if (messageHub.publish() != null)
			for (Datalake.Tank.Event tank : messageHub.publish().tanks())
				frameOf(tank).forEach(f -> builder.add("publish", f));
		if (messageHub.subscribe() != null)
			for (Datalake.Tank.Event tank : messageHub.subscribe().tanks())
				frameOf(tank).forEach(f -> builder.add("subscribe", f));
		return builder.toFrame();
	}

	private List<Frame> frameOf(Datalake.Tank.Event tank) {
		List<Frame> frames = new ArrayList<>();
		if (tank.asTank().isContextual()) {
			FrameBuilder builder = new FrameBuilder("context").add("type", tank.message().name$()).add("context", tank.asTank().asContextual().context().name$());
			builder.add("channel", tank.qn());
			Datalake.Context context = tank.asTank().asContextual().context();
			if (!context.isLeaf()) for (Datalake.Context leaf : context.leafs()) {
				FrameBuilder b = new FrameBuilder("context").add("type", tank.message().name$()).add("context", leaf.name$());
				b.add("channel", (leaf.qn().isEmpty() ? "" : leaf.qn() + ".") + tank.message().name$());
				frames.add(b.toFrame());
			}
			frames.add(builder.toFrame());
		}
		return frames;
	}


	private Template template() {
		return Formatters.customize(new MessageHubAccessorTemplate()).add("typeFormat", (value) -> {
			if (value.toString().contains(".")) return Formatters.firstLowerCase(value.toString());
			else return value;
		});
	}
}
