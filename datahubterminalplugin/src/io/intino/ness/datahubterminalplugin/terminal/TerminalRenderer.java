package io.intino.ness.datahubterminalplugin.terminal;

import io.intino.datahub.model.*;
import io.intino.datahub.model.Datalake.Tank;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.itrules.Template;
import io.intino.magritte.framework.Layer;
import io.intino.ness.datahubterminalplugin.Commons;
import io.intino.ness.datahubterminalplugin.Formatters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.intino.ness.datahubterminalplugin.Formatters.*;

class TerminalRenderer {
	private final Terminal terminal;
	private final File srcDir;
	private final String rootPackage;
	private final String ontologyPackage;

	TerminalRenderer(Terminal terminal, File srcDir, String rootPackage, String ontologyPackage) {
		this.terminal = terminal;
		this.srcDir = srcDir;
		this.rootPackage = rootPackage;
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
		if(terminal.datamarts() != null) addDatamarts(builder);
		if (terminal.publish() != null) addPublish(builder);
		if (terminal.subscribe() != null) addSubscribe(builder);
		if (terminal.bpm() != null) addBpm(builder);
		return builder.toFrame();
	}

	private void addSubscribe(FrameBuilder builder) {
		terminal.subscribe().messageTanks().forEach(tank -> builder.add("subscribe", frameOf(tank)));
		if(terminal.datamarts() != null) addSubscribeForTheDatamartEvents(builder);
		terminal.subscribe().measurementTanks().forEach(tank -> builder.add("subscribe", frameOf(tank)));
	}

	private void addSubscribeForTheDatamartEvents(FrameBuilder builder) {
		Set<String> tanksAlreadySubscribedTo = terminal.subscribe().messageTanks().stream().map(Layer::name$).collect(Collectors.toSet());
		for(Datamart datamart : terminal.datamarts().datamartNames()) {
			List<Tank.Message> tanks = datamart.entityList().stream().map(Entity::from).collect(Collectors.toList());
			for(Tank.Message tank : tanks) {
				if(tanksAlreadySubscribedTo.add(tank.name$()))
					builder.add("subscribe", frameOf(tank));
			}
		}
	}

	private void addPublish(FrameBuilder builder) {
		terminal.publish().messageTanks().forEach(tank -> builder.add("publish", frameOf(tank)));
		terminal.publish().measurementTanks().forEach(tank -> builder.add("publish", frameOf(tank)));
	}

	private void addDatamarts(FrameBuilder builder) {
		for(Datamart datamart : terminal.datamarts().datamartNames()) {
			builder.add("datamart", new FrameBuilder("datamart")
					.add("name", datamart.name$())
					.add("package", ontologyPackage + ".datamarts." + javaValidName().format(datamart.name$().toLowerCase()).toString()));
		}
	}

	private Frame[] messageFrames() {
		return messageTanks().stream()
				.map(Tank.Message::message)
				.distinct()
				.map(m -> new FrameBuilder("message")
						.add("namespace", namespace(m))
						.add("namespaceQn", namespace(m).replace(".", ""))
						.add("name", m.name$())
						.add("type", messagePackage(m) + "." + firstUpperCase(m.name$())).toFrame())
				.toArray(Frame[]::new);
	}

	private Frame[] measurementFrames() {
		return measurementTanks().stream()
				.map(Tank.Measurement::measurement)
				.distinct()
				.map(m -> new FrameBuilder("measurement")
						.add("namespace", namespace(m))
						.add("namespaceQn", namespace(m).replace(".", ""))
						.add("name", m.name$())
						.add("type", measurementPackage(m) + "." + firstUpperCase(m.name$())).toFrame())
				.toArray(Frame[]::new);
	}

	private void addBpm(FrameBuilder builder) {
		String statusQn = terminal.bpm().processStatusClass();
		String processStatusQName = statusQn.substring(statusQn.lastIndexOf(".") + 1);
		FrameBuilder bpmBuilder = new FrameBuilder("default", "bpm").
				add("type", statusQn).
				add("typeName", processStatusQName);
		bpmBuilder.add("channel", processStatusQName);
		builder.add("subscribe", bpmBuilder);
		builder.add("publish", bpmBuilder);
		builder.add("event", new FrameBuilder("event").add("name", processStatusQName).add("type", statusQn).toFrame());
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

	private Frame frameOf(Tank.Measurement tank) {
		String messagesPackage = measurementPackage(tank.measurement());
		String namespace = namespace(tank.measurement());
		return new FrameBuilder("measurement").
				add("type", messagesPackage + "." + firstUpperCase(tank.measurement().name$())).
				add("message", tank.measurement().name$()).
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

	private String measurementPackage(Measurement event) {
		String aPackage = rootPackage + ".measurements";
		if (event.core$().owner().is(Namespace.class)) aPackage = aPackage + "." + namespace(event);
		return aPackage;
	}

	private String namespace(Layer event) {
		return event.core$().owner().is(Namespace.class) ? event.core$().ownerAs(Namespace.class).qn().toLowerCase() : "";
	}

	private List<Tank.Message> messageTanks() {
		List<Tank.Message> tanks = new ArrayList<>();
		if (terminal.publish() != null) tanks.addAll(terminal.publish().messageTanks());
		if (terminal.subscribe() != null) tanks.addAll(terminal.subscribe().messageTanks());
		return tanks;
	}

	private List<Tank.Measurement> measurementTanks() {
		List<Tank.Measurement> tanks = new ArrayList<>();
		if (terminal.publish() != null) tanks.addAll(terminal.publish().measurementTanks());
		if (terminal.subscribe() != null) tanks.addAll(terminal.subscribe().measurementTanks());
		return tanks;
	}

	private Template template() {
		return Formatters.customize(new TerminalTemplate()).add("typeFormat", (value) -> {
			if (value.toString().contains(".")) return Formatters.firstLowerCase(value.toString());
			else return value;
		});
	}
}
