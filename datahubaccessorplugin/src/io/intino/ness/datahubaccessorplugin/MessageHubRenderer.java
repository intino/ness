package io.intino.ness.datahubaccessorplugin;

import io.intino.datahub.graph.Message;
import io.intino.datahub.graph.MessageHub;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.itrules.Template;

import java.io.File;

public class MessageHubRenderer {
	private final MessageHub messageHub;
	private final File srcDir;
	private final String basePackage;

	public MessageHubRenderer(MessageHub messageHub, File srcDir, String basePackage) {
		this.messageHub = messageHub;
		this.srcDir = srcDir;
		this.basePackage = basePackage;
	}

	public void render() {
		final File packageFolder = new File(srcDir, basePackage.replace(".", File.separator));
		Commons.writeFrame(packageFolder, messageHub.name$()+ "MessageHub", template().render(createMessageHubFrame()));
	}

	private Frame createMessageHubFrame() {
		FrameBuilder frame = new FrameBuilder().add("accessor").add("package", basePackage).add("name", messageHub.name$());
		if (messageHub.publish() != null)
			frame.add("publish", messageHub.publish().messages().stream().map(this::frameOf).toArray(Frame[]::new));
		if (messageHub.subscribe() != null)
			frame.add("subscribe", messageHub.subscribe().messages().stream().map(this::frameOf).toArray(Frame[]::new));
		return frame.toFrame();
	}

	private Frame frameOf(Message message) {
		return new FrameBuilder().
				add("type", message.name$()).
				add("channel", (message.isContextual() ? message.asContextual().context() + "." : "") + message.name$()).
				add("context", (message.isContextual() ? message.asContextual().context() : "")).
				toFrame();
	}

	private Template template() {
		return Formatters.customize(new MessageHubAccessorTemplate()).add("typeFormat", (value) -> {
			if (value.toString().contains(".")) return Formatters.firstLowerCase(value.toString());
			else return value;
		});
	}
}
