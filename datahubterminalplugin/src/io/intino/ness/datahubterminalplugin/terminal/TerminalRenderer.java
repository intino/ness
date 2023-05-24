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
	private final Set<Tank.Message> messageTanks = new HashSet<>();
	private final Set<Tank.Measurement> measurementTanks = new HashSet<>();
	private final Set<Tank.Resource> resourceTanks = new HashSet<>();

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

		if (terminal.publish() != null) addPublish(builder);
		if (terminal.subscribe() != null) addSubscribe(builder);
		if (terminal.bpm() != null) addBpm(builder);

		if (terminal.datamarts() != null) {
			renderDatamarts(builder);
			addSubscribeForThedevents(builder);
		}

		if(!messageTanks.isEmpty()) builder.add("message", messageFrames());
		if(!measurementTanks.isEmpty()) builder.add("measurement", measurementFrames());
		if(!resourceTanks.isEmpty()) builder.add("resource", resourceFrames());

		return builder.toFrame();
	}

	private void addSubscribe(FrameBuilder builder) {
		terminal.subscribe().messageTanks().forEach(tank -> builder.add("subscribe", frameOf(tank)));
		terminal.subscribe().measurementTanks().forEach(tank -> builder.add("subscribe", frameOf(tank)));
//		terminal.subscribe().resourceTanks().forEach(tank -> builder.add("subscribe", frameOf(tank))); TODO
	}

	private void addSubscribeForThedevents(FrameBuilder builder) {
		Set<String> tanksAlreadySubscribedTo = new HashSet<>();
		if(terminal.subscribe() != null) {
			terminal.subscribe().messageTanks().stream().map(Layer::name$).forEach(tanksAlreadySubscribedTo::add);
			terminal.subscribe().measurementTanks().stream().map(Layer::name$).forEach(tanksAlreadySubscribedTo::add);
			terminal.subscribe().resourceTanks().stream().map(Layer::name$).forEach(tanksAlreadySubscribedTo::add);
		}

		for (Datamart datamart : terminal.datamarts().list()) {
			addSubscribersForEntityEvents(builder, tanksAlreadySubscribedTo, datamart);
			addSubscribersForTimelineEvents(builder, tanksAlreadySubscribedTo, datamart);
			addSubscriberForReelEvents(builder, tanksAlreadySubscribedTo, datamart);
		}
	}

	private void addSubscriberForReelEvents(FrameBuilder builder, Set<String> tanksAlreadySubscribedTo, Datamart datamart) {
		datamart.reelList().stream()
				.flatMap(r -> r.signalList().stream())
				.map(Reel.Signal::tank)
				.filter(Objects::nonNull).distinct()
				.filter(tank -> tanksAlreadySubscribedTo.add(tank.name$()))
				.forEach(tank -> builder.add("subscribe", frameOf(tank)));
	}

	private void addSubscribersForTimelineEvents(FrameBuilder builder, Set<String> tanksAlreadySubscribedTo, Datamart datamart) {
		datamart.timelineList().stream()
				.map(Timeline::tank)
				.filter(Objects::nonNull).distinct()
				.filter(tank -> tanksAlreadySubscribedTo.add(tank.name$()))
				.forEach(tank -> builder.add("subscribe", frameOf(tank)));
	}

	private void addSubscribersForEntityEvents(FrameBuilder builder, Set<String> tanksAlreadySubscribedTo, Datamart datamart) {
		datamart.entityList().stream()
				.map(Entity::from)
				.filter(Objects::nonNull).distinct()
				.filter(tank -> tanksAlreadySubscribedTo.add(tank.name$()))
				.forEach(tank -> builder.add("subscribe", frameOf(tank)));
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
		Map<String, FrameBuilder> events = new HashMap<>();
		events.putAll(entityEventsOf(datamart));
		events.putAll(timelineEventsOf(datamart));
		events.putAll(reelEventsOf(datamart));
		return events.values().toArray(FrameBuilder[]::new);
	}

	private Map<String, FrameBuilder> reelEventsOf(Datamart datamart) {
		return datamart.reelList().stream()
				.flatMap(r -> r.signalList().stream())
				.map(s -> s.tank().message())
				.filter(Objects::nonNull)
				.distinct()
				.collect(Collectors.toMap(Layer::name$, tank -> frameOf(tank, datamart)));
	}

	private Map<String, FrameBuilder> timelineEventsOf(Datamart datamart) {
		return datamart.timelineList().stream()
				.map(Timeline::tank)
				.filter(Objects::nonNull)
				.map(Tank.Measurement::sensor)
				.distinct()
				.collect(Collectors.toMap(Layer::name$, tank -> frameOf(tank, datamart)));
	}

	private Map<String, FrameBuilder> entityEventsOf(Datamart datamart) {
		return datamart.entityList().stream()
				.map(Entity::from)
				.filter(Objects::nonNull)
				.map(Tank.Message::message)
				.distinct()
				.collect(Collectors.toMap(Layer::name$, tank -> frameOf(tank, datamart)));
	}

	private FrameBuilder frameOf(Sensor sensor, Datamart datamart) {
		return new FrameBuilder("devent")
				.add("message", sensor.name$())
				.add("namespaceQn", namespace(sensor).replace(".", ""))
				.add("datamart", datamart.name$());
	}

	private FrameBuilder frameOf(Message message, Datamart datamart) {
		return new FrameBuilder("devent")
				.add("message", message.name$())
				.add("namespaceQn", namespace(message).replace(".", ""))
				.add("datamart", datamart.name$());
	}

	private Frame[] messageFrames() {
		return messageTanks.stream()
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
		return measurementTanks.stream()
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
		return resourceTanks.stream()
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
		String namespace = Optional.ofNullable(terminal.bpm().namespace()).orElse("");
		String channel = namespace.isEmpty() ? processStatusQName : namespace + "." + processStatusQName;

		FrameBuilder bpmBuilder = new FrameBuilder("default", "bpm")
				.add("type", statusQn)
				.add("namespaceQn", statusQn)
				.add("typeName", processStatusQName)
				.add("message", processStatusQName)
				.add("channel", channel);

		builder.add("event", new FrameBuilder("event")
				.add("type", statusQn)
				.add("name", processStatusQName)
				.add("typename", processStatusQName)
				.toFrame());

		builder.add("processstatus", new FrameBuilder("event")
				.add("type", statusQn)
				.add("name", processStatusQName)
				.add("typename", processStatusQName)
				.add("channel", channel)
				.toFrame());

		builder.add("subscribe", bpmBuilder);
		builder.add("publish", bpmBuilder);
	}

	private Frame frameOf(Tank.Message tank) {
		messageTanks.add(tank);
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
		resourceTanks.add(tank);
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
		measurementTanks.add(tank);
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

	private Template template() {
		return Formatters.customize(new TerminalTemplate()).add("typeFormat", (value) -> {
			if (value.toString().contains(".")) return Formatters.firstLowerCase(value.toString());
			else return value;
		});
	}
}
