package io.intino.ness.datahubterminalplugin.terminal;

import io.intino.Configuration;
import io.intino.datahub.model.*;
import io.intino.datahub.model.Datalake.Tank;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.itrules.Template;
import io.intino.magritte.framework.Layer;
import io.intino.ness.datahubterminalplugin.Commons;
import io.intino.ness.datahubterminalplugin.Formatters;
import io.intino.ness.datahubterminalplugin.master.DatamartsRenderer;
import io.intino.plugin.PluginLauncher;

import java.io.File;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

import static io.intino.ness.datahubterminalplugin.Formatters.*;

class TerminalRenderer {
	private final Terminal terminal;
	private final File srcDir;
	private final String rootPackage;
	private final Configuration conf;
	private final PrintStream logger;
	private final PluginLauncher.Notifier notifier;
	private final String ontologyPackage;

	TerminalRenderer(Terminal terminal, File srcDir, String rootPackage, Configuration conf, PrintStream logger, PluginLauncher.Notifier notifier, String ontologyPackage) {
		this.terminal = terminal;
		this.srcDir = srcDir;
		this.rootPackage = rootPackage;
		this.conf = conf;
		this.logger = logger;
		this.notifier = notifier;
		this.ontologyPackage = ontologyPackage;
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
		builder.add("measurement", measurementFrames());
//		builder.add("resource", resourceFrames()); TODO
		if (terminal.datamarts() != null) renderDatamarts(builder);
		if (terminal.publish() != null) addPublish(builder);
		if (terminal.subscribe() != null) addSubscribe(builder);
		if (terminal.bpm() != null) addBpm(builder);
		return builder.toFrame();
	}

	private void addSubscribe(FrameBuilder builder) {
		terminal.subscribe().messageTanks().forEach(tank -> builder.add("subscribe", frameOf(tank)));
		if (terminal.datamarts() != null) addSubscribeForThedevents(builder);
		terminal.subscribe().measurementTanks().forEach(tank -> builder.add("subscribe", frameOf(tank)));
//		terminal.subscribe().resourceTanks().forEach(tank -> builder.add("subscribe", frameOf(tank))); TODO
	}

	private void addSubscribeForThedevents(FrameBuilder builder) {
		Set<String> tanksAlreadySubscribedTo = terminal.subscribe().messageTanks().stream().map(Layer::name$).collect(Collectors.toSet());
		for (Datamart datamart : terminal.datamarts().list()) {
			List<Tank.Message> tanks = datamart.entityList().stream().map(Entity::from).filter(Objects::nonNull).distinct().collect(Collectors.toList());
			for (Tank.Message tank : tanks) {
				if (tanksAlreadySubscribedTo.add(tank.name$())) {
					builder.add("subscribe", frameOf(tank));
				}
//				builder.add("devent", frameOf(tank, datamart));
			}
		}
	}

	private void addPublish(FrameBuilder builder) {
		terminal.publish().messageTanks().forEach(tank -> builder.add("publish", frameOf(tank)));
		terminal.publish().measurementTanks().forEach(tank -> builder.add("publish", frameOf(tank)));
//		terminal.publish().resourceTanks().forEach(tank -> builder.add("publish", frameOf(tank))); TODO
	}

	private void renderDatamarts(FrameBuilder builder) {
		for (Datamart datamart : terminal.datamarts().list()) {
			builder.add("datamart", frameOf(datamart));
		}
		new DatamartsRenderer(srcDir, terminal.graph(), conf, logger, notifier, ontologyPackage).render(terminal, rootPackage);
	}

	private FrameBuilder frameOf(Datamart datamart) {
		return new FrameBuilder("datamart")
				.add("name", datamart.name$())
				.add("terminal", terminal.name$())
				.add("package", ontologyPackage + ".datamarts." + javaValidName().format(datamart.name$().toLowerCase()).toString())
				.add("devent", eventsOf(datamart));
	}

	private FrameBuilder[] eventsOf(Datamart datamart) {
		return datamart.entityList().stream()
				.map(Entity::from)
				.filter(Objects::nonNull)
				.distinct()
				.map(tank -> frameOf(tank, datamart))
				.toArray(FrameBuilder[]::new);
	}

	private FrameBuilder frameOf(Tank.Message tank, Datamart datamart) {
		return new FrameBuilder("devent")
				.add("message", tank.message().name$())
				.add("namespaceQn", namespace(tank.message()).replace(".", ""))
				.add("datamart", datamart.name$());
	}

	private Frame[] messageFrames() {
		return messageTanks().stream()
				.map(Tank.Message::message)
				.distinct()
				.map(m -> new FrameBuilder("message")
						.add("namespace", namespace(m))
						.add("namespaceQn", namespace(m).replace(".", ""))
						.add("name", m.name$())
						.add("typename", firstUpperCase(m.name$()))
						.add("type", messagePackage(m) + "." + firstUpperCase(m.name$())).toFrame())
				.toArray(Frame[]::new);
	}

	private Frame[] measurementFrames() {
		return measurementTanks().stream()
				.map(Tank.Measurement::sensor)
				.distinct()
				.map(m -> new FrameBuilder("measurement")
						.add("namespace", namespace(m))
						.add("namespaceQn", namespace(m).replace(".", ""))
						.add("name", m.name$())
						.add("typename", firstUpperCase(m.name$()))
						.add("type", measurementPackage(m) + "." + firstUpperCase(m.name$())).toFrame())
				.toArray(Frame[]::new);
	}

	private Frame[] resourceFrames() {
		return resourceTanks().stream()
				.map(Tank.Resource::resourceEvent)
				.distinct()
				.map(r -> new FrameBuilder("resource")
						.add("namespace", namespace(r))
						.add("namespaceQn", namespace(r).replace(".", ""))
						.add("name", r.name$())
						.add("typename", firstUpperCase(r.name$()))
						.add("type", resourcePackage(r) + "." + firstUpperCase(r.name$())).toFrame())
				.toArray(Frame[]::new);
	}

	private void addBpm(FrameBuilder builder) {
		String statusQn = terminal.bpm().processStatusClass();
		String processStatusQName = statusQn.substring(statusQn.lastIndexOf(".") + 1);
		FrameBuilder bpmBuilder = new FrameBuilder("default", "bpm")
				.add("type", statusQn)
				.add("namespaceQn", statusQn)
				.add("typeName", processStatusQName)
				.add("message", processStatusQName)
				.add("channel", processStatusQName);

		builder.add("event", new FrameBuilder("event")
				.add("type", statusQn)
				.add("name", processStatusQName)
				.add("typename", processStatusQName)
				.toFrame());

		builder.add("processstatus", new FrameBuilder("event")
				.add("type", statusQn)
				.add("name", processStatusQName)
				.add("typename", processStatusQName)
				.toFrame());

		builder.add("subscribe", bpmBuilder);
		builder.add("publish", bpmBuilder);
	}

	private Frame frameOf(Tank.Message tank) {
		String messagesPackage = messagePackage(tank.message());
		String namespace = namespace(tank.message());
		return new FrameBuilder("message").
				add("type", messagesPackage + "." + firstUpperCase(tank.message().name$())).
				add("message", tank.message().name$()).
				add("typeName", tank.name$()).
				add("namespace", namespace).
				add("namespaceQn", namespace.replace(".", "")).
				add("typeWithNamespace", (namespace.isEmpty() ? "" : namespace + ".") + firstUpperCase(tank.name$())).
				add("channel", tank.qn()).toFrame();
	}

	private Frame frameOf(Tank.Resource tank) {
		String messagesPackage = resourcePackage(tank.resourceEvent());
		String namespace = namespace(tank.resourceEvent());
		return new FrameBuilder("measurement").
				add("type", messagesPackage + "." + firstUpperCase(tank.resourceEvent().name$())).
				add("message", tank.resourceEvent().name$()).
				add("typeName", tank.name$()).
				add("namespace", namespace).
				add("namespaceQn", namespace.replace(".", "")).
				add("typeWithNamespace", (namespace.isEmpty() ? "" : namespace + ".") + firstUpperCase(tank.name$())).
				add("channel", tank.qn()).toFrame();
	}

	private Frame frameOf(Tank.Measurement tank) {
		String messagesPackage = measurementPackage(tank.sensor());
		String namespace = namespace(tank.sensor());
		return new FrameBuilder("measurement").
				add("type", messagesPackage + "." + firstUpperCase(tank.sensor().name$())).
				add("message", tank.sensor().name$()).
				add("typeName", tank.name$()).
				add("namespace", namespace).
				add("namespaceQn", namespace.replace(".", "")).
				add("typeWithNamespace", (namespace.isEmpty() ? "" : namespace + ".") + firstUpperCase(tank.name$())).
				add("channel", tank.qn()).toFrame();
	}

	private String messagePackage(Message event) {
		String aPackage = rootPackage + ".messages";
		if (event.core$().owner().is(Namespace.class)) aPackage = aPackage + "." + namespace(event);
		return aPackage;
	}

	private String measurementPackage(Sensor event) {
		String aPackage = rootPackage + ".measurements";
		if (event.core$().owner().is(Namespace.class)) aPackage = aPackage + "." + namespace(event);
		return aPackage;
	}

	private String resourcePackage(Resource event) {
		String aPackage = rootPackage + ".resources";
		if (event.core$().owner().is(Namespace.class)) aPackage = aPackage + "." + namespace(event);
		return aPackage;
	}

	private String namespace(Layer event) {
		return event.core$().owner().is(Namespace.class) ? event.core$().ownerAs(Namespace.class).qn().toLowerCase() : "";
	}

	private Collection<Tank.Message> messageTanks() {
		Collection<Tank.Message> tanks = new LinkedHashSet<>();
		if (terminal.publish() != null) tanks.addAll(terminal.publish().messageTanks());
		if (terminal.subscribe() != null) tanks.addAll(terminal.subscribe().messageTanks());
		if (terminal.datamarts() != null) tanks.addAll(messageTanksOf(terminal.datamarts()));
		return tanks;
	}

	private Collection<Tank.Message> messageTanksOf(Terminal.Datamarts datamarts) {
		return datamarts.list().stream()
				.flatMap(d -> d.entityList().stream())
				.map(Entity::from)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
	}

	private List<Tank.Measurement> measurementTanks() {
		List<Tank.Measurement> tanks = new ArrayList<>();
		if (terminal.publish() != null) tanks.addAll(terminal.publish().measurementTanks());
		if (terminal.subscribe() != null) tanks.addAll(terminal.subscribe().measurementTanks());
		return tanks;
	}

	private List<Tank.Resource> resourceTanks() {
		List<Tank.Resource> tanks = new ArrayList<>();
		if (terminal.publish() != null) tanks.addAll(terminal.publish().resourceTanks());
		if (terminal.subscribe() != null) tanks.addAll(terminal.subscribe().resourceTanks());
		return tanks;
	}

	private Template template() {
		return Formatters.customize(new TerminalTemplate()).add("typeFormat", (value) -> {
			if (value.toString().contains(".")) return Formatters.firstLowerCase(value.toString());
			else return value;
		});
	}
}
