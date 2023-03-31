package io.intino.ness.datahubterminalplugin.ontology;

import io.intino.Configuration;
import io.intino.datahub.model.*;
import io.intino.ness.datahubterminalplugin.master.DatamartsRenderer;
import io.intino.ness.datahubterminalplugin.measurement.MeasurementRenderer;
import io.intino.ness.datahubterminalplugin.message.MessageRenderer;
import io.intino.ness.datahubterminalplugin.resource.ResourceRenderer;
import io.intino.plugin.PluginLauncher;

import java.io.File;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class OntologyRenderer {
	private final NessGraph graph;
	private final Configuration conf;
	private final PrintStream logger;
	private final PluginLauncher.Notifier notifier;
	private final File srcDir;
	private final String basePackage;

	OntologyRenderer(NessGraph graph, Configuration conf, File srcDir, String basePackage, PrintStream logger, PluginLauncher.Notifier notifier) {
		this.graph = graph;
		this.conf = conf;
		this.logger = logger;
		this.notifier = notifier;
		this.srcDir = srcDir;
		this.basePackage = basePackage;
		srcDir.mkdirs();
	}

	public boolean render() {
		renderMessages();
		renderMeasurements();
//		renderResources(); TODO
		renderDatamarts();
		return true;
	}

	private void renderMessages() {
		graph.core$().find(Message.class).forEach(event -> new MessageRenderer(event, srcDir, basePackage).render());
	}

	private void renderMeasurements() {
		measurements().forEach(m -> new MeasurementRenderer(m, srcDir, basePackage).render());
	}

	private void renderResources() {
		graph.core$().find(Resource.class).forEach(r -> new ResourceRenderer(r, srcDir, basePackage).render());
	}

	private void renderDatamarts() {
		new DatamartsRenderer(srcDir, graph, conf, logger, notifier, basePackage).render();
	}

	private List<Measurement> measurements() {
		return graph.datalake() == null ? Collections.emptyList() : graph.datalake().tankList().stream()
				.filter(Datalake.Tank::isMeasurement)
				.map(tank -> tank.asMeasurement().measurement())
				.collect(Collectors.toList());
	}
}