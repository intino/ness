package io.intino.ness.datahubterminalplugin.ontology;

import io.intino.Configuration;
import io.intino.datahub.model.Datalake;
import io.intino.datahub.model.Message;
import io.intino.datahub.model.NessGraph;
import io.intino.ness.datahubterminalplugin.master.MasterRenderer;
import io.intino.ness.datahubterminalplugin.measurement.MeasurementRenderer;
import io.intino.ness.datahubterminalplugin.message.MessageRenderer;
import io.intino.plugin.PluginLauncher;

import java.io.File;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

public class OntologyRenderer {
	private final List<Message> messages;
	private final NessGraph graph;
	private final Configuration conf;
	private final PrintStream logger;
	private final PluginLauncher.Notifier notifier;
	private final Map<Message, Datalake.Split> messagesSplitMap;
	private final File srcDir;
	private final String basePackage;

	OntologyRenderer(NessGraph graph, Configuration conf, File srcDir, String basePackage, PrintStream logger, PluginLauncher.Notifier notifier) {
		this.messages = graph.messageList();
		this.graph = graph;
		this.conf = conf;
		this.logger = logger;
		this.notifier = notifier;
		this.messagesSplitMap = splitMessages();
		this.srcDir = srcDir;
		this.basePackage = basePackage;
		srcDir.mkdirs();
	}

	public boolean render() {
		renderMessages();
		renderMeasurements();
		renderEntities();
		return true;
	}

	private void renderMeasurements() {
		measurementTanks().forEach(k -> new MeasurementRenderer(k.measurement(), srcDir, basePackage).render());
	}

	private void renderEntities() {
		new MasterRenderer(srcDir, graph, conf, logger, notifier, basePackage).renderOntology();
	}

	private void renderMessages() {
		messagesSplitMap.forEach((k, v) -> new MessageRenderer(k, v, srcDir, basePackage).render());
		messages.stream().filter(event -> !messagesSplitMap.containsKey(event)).parallel().forEach(event -> new MessageRenderer(event, null, srcDir, basePackage).render());
	}


	private Map<Message, Datalake.Split> splitMessages() {
		Map<Message, Datalake.Split> events = new HashMap<>();
		for (Datalake.Tank.Message tank : messageTanks()) {
			List<Message> hierarchy = hierarchy(tank.message());
			Datalake.Split split = tank.asTank().isSplitted() ? tank.asTank().asSplitted().split() : null;
			events.put(hierarchy.get(0), split);
			hierarchy.remove(0);
			hierarchy.forEach(e -> events.put(e, null));
		}
		return events;
	}

	private List<Datalake.Tank.Message> messageTanks() {
		return graph.datalake() == null ? Collections.emptyList() : graph.datalake().tankList().stream()
				.filter(Datalake.Tank::isMessage)
				.map(Datalake.Tank::asMessage)
				.collect(Collectors.toList());
	}


	private List<Datalake.Tank.Measurement> measurementTanks() {
		return graph.datalake() == null ? Collections.emptyList() : graph.datalake().tankList().stream()
				.filter(Datalake.Tank::isMeasurement)
				.map(Datalake.Tank::asMeasurement)
				.collect(Collectors.toList());
	}

	private List<Message> hierarchy(Message event) {
		Set<Message> events = new LinkedHashSet<>();
		events.add(event);
		if (event.isExtensionOf()) events.addAll(hierarchy(event.asExtensionOf().parent()));
		return new ArrayList<>(events);
	}
}
