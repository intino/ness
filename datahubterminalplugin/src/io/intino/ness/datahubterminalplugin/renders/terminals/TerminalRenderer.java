package io.intino.ness.datahubterminalplugin.renders.terminals;

import io.intino.datahub.graph.*;
import io.intino.datahub.graph.Datalake.Split;
import io.intino.datahub.graph.Datalake.Tank;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.itrules.Template;
import io.intino.ness.datahubterminalplugin.renders.Formatters;

import java.io.File;
import java.util.*;

import static io.intino.ness.datahubterminalplugin.Commons.writeFrame;
import static io.intino.ness.datahubterminalplugin.renders.Formatters.firstUpperCase;

public class TerminalRenderer {
	private final Terminal terminal;
	private final Map<Event, Split> eventWithSplit;
	private final File srcDir;
	private final String rootPackage;

	public TerminalRenderer(Terminal terminal, Map<Event, Split> eventWithSplit, File srcDir, String rootPackage) {
		this.terminal = terminal;
		this.eventWithSplit = eventWithSplit;
		this.srcDir = srcDir;
		this.rootPackage = rootPackage;
	}

	public void render() {
		final File packageFolder = new File(srcDir, rootPackage.replace(".", File.separator));
		writeLookups(packageFolder);
		writeTerminal(packageFolder);
	}

	private void writeTerminal(File packageFolder) {
		writeFrame(packageFolder, Formatters.snakeCaseToCamelCase().format(terminal.name$()).toString(), terminalTemplate().render(createTerminalFrame()));
	}

	private void writeLookups(File packageFolder) {
		List<Lookup.Dynamic> lookups = terminal.core$().graph().find(Lookup.Dynamic.class);
		if (!lookups.isEmpty()) writeFrame(packageFolder, "Lookups", lookupsTemplate().render(createLookups(lookups)));
	}

	private Frame createTerminalFrame() {
		Datalake datalake = terminal.graph().datalake();
		FrameBuilder builder = new FrameBuilder("terminal").add("package", rootPackage).add("name", terminal.name$());
		if (datalake != null) builder.add("datalake", "").add("scale", datalake.scale().name());
		builder.add("event", eventWithSplit.keySet().stream().map(e -> new FrameBuilder("event").
				add("namespace", eventNamespace(e)).
				add("namespaceQn", eventNamespace(e).replace(".", "")).
				add("name", e.name$()).
				add("type", eventPackage(e) + "." + firstUpperCase(e.name$())).toFrame()).toArray(Frame[]::new));
		terminal.core$().graph().find(Transaction.class).forEach(t -> renderTransaction(builder, t));
		terminal.core$().graph().find(Lookup.Dynamic.class).forEach(l -> renderLookup(builder, l));
		if (terminal.publish() != null)
			terminal.publish().tanks().forEach(tank -> builder.add("publish", frameOf(tank)));
		if (terminal.subscribe() != null)
			terminal.subscribe().tanks().forEach(tank -> builder.add("subscribe", frameOf(tank)));
		if (terminal.bpm() != null) addBpm(builder);
		return builder.toFrame();
	}

	private void renderLookup(FrameBuilder fb, Lookup.Dynamic l) {
		renderLookup(rootPackage + ".lookups", fb, l);
	}

	private Frame createLookups(List<Lookup.Dynamic> lookups) {
		Set<String> namespaces = new LinkedHashSet<>();
		FrameBuilder fb = new FrameBuilder("lookups");
		fb.add("package", rootPackage);
		for (Lookup.Dynamic l : lookups) {
			renderLookup(rootPackage + ".lookups", fb, l);
			namespaces.add(l.namespace());
		}
		fb.add("namespace", namespaces.toArray(new String[0]));
		return fb.toFrame();
	}

	private void renderLookup(String lookupsPackage, FrameBuilder fb, Lookup.Dynamic l) {
		fb.add("lookup", new FrameBuilder("lookup").
				add("qn", lookupsPackage + "." + firstUpperCase(l.name$())).
				add("namespace", l.namespace()).
				add("name", l.name$()));
	}

	private void renderTransaction(FrameBuilder builder, Transaction t) {
		String transactionsPackage = rootPackage + ".transactions";
		if (t.core$().owner().is(Namespace.class))
			transactionsPackage = transactionsPackage + "." + t.core$().ownerAs(Namespace.class).qn();
		builder.add("transaction", new FrameBuilder("transaction").
				add("qn", transactionsPackage + "." + firstUpperCase(t.name$())).
				add("namespace", t.core$().owner().is(Namespace.class) ? t.core$().ownerAs(Namespace.class).qn().replace(".", "") : "").
				add("name", t.name$()));
	}

	private void addBpm(FrameBuilder builder) {
		Split split = terminal.bpm().split();
		List<Split> leafs = new ArrayList<>();
		if (split != null) {
			leafs.addAll(split.isLeaf() ? Collections.singletonList(split) : split.leafs());
			builder.add("bpm", new FrameBuilder("bpm").add("split", enums(split, leafs)));
		}
		String statusQn = terminal.bpm().processStatusClass();
		String processStatusQName = statusQn.substring(statusQn.lastIndexOf(".") + 1);
		FrameBuilder bpmBuilder = new FrameBuilder(leafs.size() > 1 ? "multisplit" : "default", "bpm").
				add("type", statusQn).
				add("typeName", processStatusQName);
		if (leafs.size() <= 1)
			bpmBuilder.add("channel", (leafs.size() == 1 ? leafs.get(0).qn() + "." : "") + processStatusQName);
		builder.add("subscribe", bpmBuilder);
		builder.add("publish", bpmBuilder);
		builder.add("event", new FrameBuilder("event").add("name", processStatusQName).add("type", statusQn).toFrame());
	}

	private Frame[] enums(Split realSplit, List<Split> leafs) {
		List<Frame> frames = new ArrayList<>();
		if (!leafs.contains(realSplit) && !realSplit.label().isEmpty())
			frames.add(new FrameBuilder("enum").add("value", realSplit.qn().replace(".", "-")).toFrame());
		for (Split leaf : leafs) {
			FrameBuilder builder = new FrameBuilder("enum").add("value", leaf.qn().replace(".", "-")).add("qn", leaf.qn());
			frames.add(builder.toFrame());
		}
		return frames.toArray(new Frame[0]);
	}

	private Frame frameOf(Tank.Event eventTank) {
		String eventPackage = eventPackage(eventTank.event());
		String namespace = eventNamespace(eventTank.event());
		return new FrameBuilder(contextsOf(eventTank).size() > 1 ? "multisplit" : "default").
				add("type", eventPackage + "." + firstUpperCase(eventTank.event().name$())).
				add("typeName", eventTank.event().name$()).
				add("namespace", namespace).
				add("namespaceQn", namespace.replace(".", "")).
				add("typeWithNamespace", (namespace.isEmpty() ? "" : namespace + ".") + firstUpperCase(eventTank.event().name$())).
				add("channel", eventTank.qn()).toFrame();
	}

	private String eventPackage(Event event) {
		String eventPackage = eventsPackage();
		if (event.core$().owner().is(Namespace.class))
			eventPackage = eventPackage + "." + eventNamespace(event);
		return eventPackage;
	}

	private String eventNamespace(Event event) {
		return event.core$().owner().is(Namespace.class) ? event.core$().ownerAs(Namespace.class).qn().toLowerCase() : "";
	}

	private String eventsPackage() {
		return rootPackage + ".events";
	}

	private List<Split> contextsOf(Tank.Event tank) {
		return tank.asTank().isSplitted() ? tank.asTank().asSplitted().split().leafs() : Collections.emptyList();
	}

	private Template terminalTemplate() {
		return Formatters.customize(new TerminalTemplate()).add("typeFormat", (value) -> {
			if (value.toString().contains(".")) return Formatters.firstLowerCase(value.toString());
			else return value;
		});
	}

	private Template lookupsTemplate() {
		return Formatters.customize(new LookupsTemplate()).add("typeFormat", (value) -> {
			if (value.toString().contains(".")) return Formatters.firstLowerCase(value.toString());
			else return value;
		});
	}
}
