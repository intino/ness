package io.intino.ness.datahubterminalplugin;

import io.intino.datahub.graph.Datalake;
import io.intino.datahub.graph.Datalake.Context;
import io.intino.datahub.graph.Datalake.Tank;
import io.intino.datahub.graph.Event;
import io.intino.datahub.graph.Namespace;
import io.intino.datahub.graph.Terminal;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.itrules.Template;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

class TerminalRenderer {
	private final Terminal terminal;
	private final Map<Event, Context> eventWithContext;
	private final File srcDir;
	private final String rootPackage;

	TerminalRenderer(Terminal terminal, Map<Event, Context> eventWithContext, File srcDir, String rootPackage) {
		this.terminal = terminal;
		this.eventWithContext = eventWithContext;
		this.srcDir = srcDir;
		this.rootPackage = rootPackage;
	}

	void render() {
		final File packageFolder = new File(srcDir, rootPackage.replace(".", File.separator));
		Commons.writeFrame(packageFolder, Formatters.snakeCaseToCamelCase().format(terminal.name$()).toString(), template().render(createTerminalFrame()));
	}

	private Frame createTerminalFrame() {
		Datalake datalake = terminal.graph().datalake();
		FrameBuilder builder = new FrameBuilder("terminal").add("package", rootPackage).add("name", terminal.name$());
		if (datalake != null) builder.add("datalake", "").add("scale", datalake.scale().name());
		builder.add("event", eventWithContext.keySet().stream().map(e -> new FrameBuilder("event").add("namespace", eventNamespace(e)).add("name", e.name$()).add("type", eventPackage(e) + "." + Formatters.firstUpperCase(e.name$())).toFrame()).toArray(Frame[]::new));
		if (terminal.publish() != null)
			terminal.publish().tanks().forEach(tank -> builder.add("publish", frameOf(tank)));
		if (terminal.subscribe() != null)
			terminal.subscribe().tanks().forEach(tank -> builder.add("subscribe", frameOf(tank)));
		if (terminal.allowsBpmIn() != null) addBpm(builder);
		return builder.toFrame();
	}

	private void addBpm(FrameBuilder builder) {
		Context context = terminal.allowsBpmIn().context();
		List<Context> leafs = new ArrayList<>();
		if (context != null) {
			leafs.addAll(context.isLeaf() ? Collections.singletonList(context) : context.leafs());
			builder.add("bpm", new FrameBuilder("bpm").add("context", enums(context, leafs)));
		}
		String statusQn = terminal.allowsBpmIn().processStatusClass();
		String processStatusQName = statusQn.substring(statusQn.lastIndexOf(".") + 1);
		FrameBuilder bpmBuilder = new FrameBuilder(leafs.size() > 1 ? "multicontext" : "default", "bpm").
				add("type", statusQn).
				add("typeName", processStatusQName);
		if (leafs.size() <= 1)
			bpmBuilder.add("channel", (leafs.size() == 1 ? leafs.get(0).qn() + "." : "") + processStatusQName);
		builder.add("subscribe", bpmBuilder);
		builder.add("publish", bpmBuilder);
		builder.add("event", new FrameBuilder("event").add("name", processStatusQName).add("type", statusQn).toFrame());
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

	private Frame frameOf(Tank.Event eventTank) {
		String eventPackage = eventPackage(eventTank.event());
		String namespace = eventNamespace(eventTank.event());
		return new FrameBuilder(contextsOf(eventTank).size() > 1 ? "multicontext" : "default").
				add("type", eventPackage + "." + Formatters.firstUpperCase(eventTank.event().name$())).
				add("typeName", eventTank.event().name$()).
				add("namespace", namespace).
				add("typeWithNamespace", (namespace.isEmpty() ? "" : namespace + ".") + Formatters.firstUpperCase(eventTank.event().name$())).
				add("channel", eventTank.qn()).toFrame();
	}

	private String eventPackage(Event event) {
		String eventPackage = eventsPackage();
		if (event.core$().owner().is(Namespace.class))
			eventPackage = eventPackage + "." + eventNamespace(event);
		return eventPackage;
	}

	private String eventNamespace(Event event) {
		return event.core$().owner().is(Namespace.class) ? event.core$().ownerAs(Namespace.class).name$().toLowerCase() : "";
	}

	private String eventsPackage() {
		return rootPackage + ".events";
	}

	private List<Context> contextsOf(Tank.Event tank) {
		return tank.asTank().isContextual() ? tank.asTank().asContextual().context().leafs() : Collections.emptyList();
	}

	private Template template() {
		return Formatters.customize(new TerminalTemplate()).add("typeFormat", (value) -> {
			if (value.toString().contains(".")) return Formatters.firstLowerCase(value.toString());
			else return value;
		});
	}
}
