package io.intino.ness.builder.codegeneration.terminal;

import io.intino.builder.CompilerConfiguration;
import io.intino.datahub.model.*;
import io.intino.datahub.model.Datalake.Tank;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.itrules.Template;
import io.intino.magritte.framework.Layer;
import io.intino.ness.builder.IntinoException;
import io.intino.ness.builder.codegeneration.Commons;
import io.intino.ness.builder.codegeneration.Formatters;
import io.intino.ness.builder.codegeneration.datamarts.DatamartsRenderer;
import io.intino.ness.builder.codegeneration.datamarts.TimelineUtils;

import java.io.File;
import java.util.*;

import static io.intino.ness.builder.codegeneration.Formatters.*;
import static java.util.stream.Collectors.toMap;

class TerminalRenderer {
	private final Terminal terminal;
	private final File srcDir;
	private final String rootPackage;
	private final CompilerConfiguration configuration;
	private final String ontologyPackage;
	private final Set<Tank.Message> messageTanks = new HashSet<>();
	private final Set<Tank.Measurement> measurementTanks = new HashSet<>();
	private final Set<Tank.Resource> resourceTanks = new HashSet<>();
	private final Datalake datalake;

	TerminalRenderer(Terminal terminal, File srcDir, String rootPackage, CompilerConfiguration configuration) {
		this.terminal = terminal;
		this.datalake = terminal.graph().datalake();
		this.srcDir = srcDir;
		this.rootPackage = rootPackage;
		this.configuration = configuration;
		this.ontologyPackage = configuration.groupId().toLowerCase() + "." + Formatters.snakeCaseToCamelCase().format(configuration.artifactId()).toString().toLowerCase();
	}

	void render() throws IntinoException {
		final File packageFolder = new File(srcDir, rootPackage.replace(".", File.separator));
		Commons.writeFrame(packageFolder, snakeCaseToCamelCase().format(terminal.name$()).toString(), template().render(createTerminalFrame()));
	}

	private Frame createTerminalFrame() throws IntinoException {
		Datalake datalake = terminal.graph().datalake();
		FrameBuilder builder = new FrameBuilder("terminal").add("package", rootPackage).add("name", terminal.name$());
		if (datalake != null) builder.add("datalake", "").add("scale", datalake.scale().name());

		if (terminal.publish() != null) addPublish(builder);
		if (terminal.subscribe() != null) addSubscribe(builder);
		if (terminal.bpm() != null) addBpm(builder);

		if (terminal.datamarts() != null) {
			renderDatamarts(builder);
			addSubscribeForTheEvents(builder);
		}

		if (!messageTanks.isEmpty()) builder.add("message", messageFrames());
		if (!measurementTanks.isEmpty()) builder.add("measurement", measurementFrames());
		if (!resourceTanks.isEmpty()) builder.add("resource", resourceFrames());
		return builder.toFrame();
	}

	private void addSubscribe(FrameBuilder builder) {
		terminal.subscribe().messageTanks().forEach(tank -> builder.add("subscribe", frameOf(tank)));
		terminal.subscribe().measurementTanks().forEach(tank -> builder.add("subscribe", frameOf(tank)));
		terminal.subscribe().resourceTanks().forEach(tank -> builder.add("subscribe", frameOf(tank)));
	}

	private void addSubscribeForTheEvents(FrameBuilder builder) {
		Set<String> tanksAlreadySubscribedTo = new HashSet<>();
		if (terminal.subscribe() != null) {
			terminal.subscribe().messageTanks().stream().map(Tank.Message::qn).forEach(tanksAlreadySubscribedTo::add);
			terminal.subscribe().measurementTanks().stream().map(Tank.Measurement::qn).forEach(tanksAlreadySubscribedTo::add);
			terminal.subscribe().resourceTanks().stream().map(Tank.Resource::qn).forEach(tanksAlreadySubscribedTo::add);
		}

		for (Datamart datamart : terminal.datamarts().list()) {
			addSubscribersForEntityEvents(builder, tanksAlreadySubscribedTo, datamart);
			addSubscribersForTimelineEvents(builder, tanksAlreadySubscribedTo, datamart);
			addSubscriberForReelEvents(builder, tanksAlreadySubscribedTo, datamart);
		}
	}

	private void addSubscriberForReelEvents(FrameBuilder builder, Set<String> tanksAlreadySubscribedTo, Datamart datamart) {
		datamart.reelList().stream()
				.map(Reel::tank)
				.filter(Objects::nonNull)
				.distinct()
				.filter(tank -> tanksAlreadySubscribedTo.add(tank.qn()))
				.forEach(tank -> builder.add("subscribe", frameOf(tank)));
	}

	private void addSubscribersForTimelineEvents(FrameBuilder builder, Set<String> tanksAlreadySubscribedTo, Datamart datamart) {
		datamart.timelineList().stream().collect(toMap(t -> t, t -> TimelineUtils.tanksOf(t).toList())).values().stream()
				.flatMap(Collection::stream)
				.distinct()
				.filter(tanksAlreadySubscribedTo::add)
				.forEach(tank -> builder.add("subscribe", frameOf(findTankByQn(tank))));
	}


	private void addSubscribersForEntityEvents(FrameBuilder builder, Set<String> tanksAlreadySubscribedTo, Datamart datamart) {
		datamart.entityList().stream()
				.map(Entity::from)
				.filter(Objects::nonNull)
				.distinct()
				.filter(tank -> tanksAlreadySubscribedTo.add(tank.qn()))
				.forEach(tank -> builder.add("subscribe", frameOf(tank)));
	}

	private Frame frameOf(Tank tank) {
		return tank.isMessage() ? frameOf(tank.asMessage()) : frameOf(tank.asMeasurement());
	}

	private Tank findTankByQn(String tank) {
		return datalake.tank(t -> t.qn().equals(tank));
	}

	private void addPublish(FrameBuilder builder) {
		terminal.publish().messageTanks().forEach(tank -> builder.add("publish", frameOf(tank)));
		terminal.publish().measurementTanks().forEach(tank -> builder.add("publish", frameOf(tank)));
		terminal.publish().resourceTanks().forEach(tank -> builder.add("publish", frameOf(tank))); // TODO resources
	}

	private void renderDatamarts(FrameBuilder builder) throws IntinoException {
		for (Datamart datamart : terminal.datamarts().list())
			builder.add("datamart", frameOf(datamart));
		new DatamartsRenderer(srcDir, terminal.graph(), configuration, ontologyPackage).render(terminal, rootPackage);
	}

	private FrameBuilder frameOf(Datamart datamart) {
		return new FrameBuilder("datamart")
				.add("name", datamart.name$())
				.add("terminal", terminal.name$())
				.add("package", ontologyPackage + ".datamarts." + javaValidName().format(datamart.name$().toLowerCase()).toString())
				.add("devent", eventsOf(datamart));
	}

	private FrameBuilder[] eventsOf(Datamart datamart) {
		Map<String, FrameBuilder> events = new HashMap<>(entityEventsOf(datamart));
		return events.values().toArray(FrameBuilder[]::new);
	}

	private Map<String, FrameBuilder> entityEventsOf(Datamart datamart) {
		return datamart.entityList().stream()
				.map(Entity::from)
				.filter(Objects::nonNull)
				.map(Tank.Message::message)
				.distinct()
				.collect(toMap(Layer::name$, tank -> frameOf(tank, datamart)));
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
				add("typeName", tank.message().name$()).
				add("namespace", namespace).
				add("namespaceQn", namespace.replace(".", "")).
				add("typeWithNamespace", (namespace.isEmpty() ? "" : namespace + ".") + firstUpperCase(tank.name$())).
				add("channel", tank.qn()).toFrame();
	}

	private Frame frameOf(Tank.Resource tank) {
		resourceTanks.add(tank);
		String messagesPackage = resourcePackage(tank.resourceEvent());
		String namespace = namespace(tank.resourceEvent());
		return new FrameBuilder("resource").
				add("type", messagesPackage + "." + firstUpperCase(tank.resourceEvent().name$())).
				add("message", tank.resourceEvent().name$()).
				add("typeName", tank.resourceEvent().name$()).
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
				add("typeName", tank.sensor().name$()).
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
