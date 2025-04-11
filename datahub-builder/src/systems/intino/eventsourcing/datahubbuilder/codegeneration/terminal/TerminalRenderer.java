package systems.intino.eventsourcing.datahubbuilder.codegeneration.terminal;

import io.intino.builder.CompilerConfiguration;
import io.intino.itrules.Engine;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.magritte.framework.Layer;
import systems.intino.eventsourcing.datahub.model.*;
import systems.intino.eventsourcing.datahub.model.Datalake.Tank;
import systems.intino.eventsourcing.datahubbuilder.IntinoException;
import systems.intino.eventsourcing.datahubbuilder.codegeneration.Commons;
import systems.intino.eventsourcing.datahubbuilder.codegeneration.Formatters;
import systems.intino.eventsourcing.datahubbuilder.codegeneration.datamarts.DatamartsRenderer;

import java.io.File;
import java.util.*;

import static java.util.stream.Collectors.toMap;
import static systems.intino.eventsourcing.datahubbuilder.codegeneration.Formatters.*;

class TerminalRenderer {
	private final Terminal terminal;
	private final File srcDir;
	private final String rootPackage;
	private final CompilerConfiguration configuration;
	private final String ontologyPackage;
	private final Set<Tank.Message> messageTanks = new HashSet<>();
	private final Set<Tank.Resource> resourceTanks = new HashSet<>();

	TerminalRenderer(Terminal terminal, File srcDir, String rootPackage, CompilerConfiguration configuration) {
		this.terminal = terminal;
		this.srcDir = srcDir;
		this.rootPackage = rootPackage;
		this.configuration = configuration;
		this.ontologyPackage = configuration.groupId().toLowerCase() + "." + Formatters.snakeCaseToCamelCase().format(configuration.artifactId()).toString().toLowerCase();
	}

	void render() throws IntinoException {
		final File packageFolder = new File(srcDir, rootPackage.replace(".", File.separator));
		Commons.writeFile(packageFolder, snakeCaseToCamelCase().format(terminal.name$()).toString(), template().render(createTerminalFrame()));
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
		if (!resourceTanks.isEmpty()) builder.add("resource", resourceFrames());
		return builder.toFrame();
	}

	private void addSubscribe(FrameBuilder builder) {
		terminal.subscribe().messageTanks().forEach(tank -> builder.add("subscribe", frameOf(tank)));
		terminal.subscribe().resourceTanks().forEach(tank -> builder.add("subscribe", frameOf(tank)));
	}

	private void addSubscribeForTheEvents(FrameBuilder builder) {
		Set<String> tanksAlreadySubscribedTo = new HashSet<>();
		if (terminal.subscribe() != null) {
			terminal.subscribe().messageTanks().stream().map(Tank.Message::qn).forEach(tanksAlreadySubscribedTo::add);
			terminal.subscribe().resourceTanks().stream().map(Tank.Resource::qn).forEach(tanksAlreadySubscribedTo::add);
		}

		terminal.datamarts().list()
				.forEach(datamart -> addSubscribersForSubject(builder, tanksAlreadySubscribedTo, datamart));
	}

	private void addSubscribersForSubject(FrameBuilder builder, Set<String> tanksAlreadySubscribedTo, Datamart datamart) {
		datamart.subjectList().stream()
				.flatMap(s -> s.from().stream())
				.distinct()
				.filter(tank -> tanksAlreadySubscribedTo.add(tank.qn()))
				.forEach(tank -> builder.add("subscribe", frameOf(tank)));
	}

	private void addPublish(FrameBuilder builder) {
		terminal.publish().messageTanks().forEach(tank -> builder.add("publish", frameOf(tank)));
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
		Map<String, FrameBuilder> events = new HashMap<>(subjectEventsOf(datamart));
		return events.values().toArray(FrameBuilder[]::new);
	}

	private Map<String, FrameBuilder> subjectEventsOf(Datamart datamart) {
		return datamart.subjectList().stream()
				.flatMap(s -> s.from().stream())
				.distinct()
				.filter(Objects::nonNull)
				.map(Tank.Message::message)
				.distinct()
				.collect(toMap(t -> t.core$().id(), tank -> frameOf(tank, datamart)));
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

	private String messagePackage(Message event) {
		String aPackage = rootPackage + ".messages";
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

	private Engine template() {
		return customize(new Engine(new TerminalTemplate())).add("typeFormat", (value) -> {
			if (value.toString().contains(".")) return Formatters.firstLowerCase(value.toString());
			else return value;
		});
	}
}