package io.intino.ness.terminal.builder.codegeneration.ontology;

import io.intino.datahub.model.*;
import io.intino.ness.terminal.builder.IntinoException;
import io.intino.ness.terminal.builder.codegeneration.datamarts.DatamartsRenderer;
import io.intino.ness.terminal.builder.codegeneration.measurement.MeasurementRenderer;
import io.intino.ness.terminal.builder.codegeneration.message.MessageRenderer;
import io.intino.ness.terminal.builder.codegeneration.resource.ResourceRenderer;
import io.intino.plugin.CompilerConfiguration;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class OntologyRenderer {
	private final NessGraph graph;
	private final CompilerConfiguration configuration;
	private final File srcDir;
	private final String basePackage;

	OntologyRenderer(NessGraph graph, CompilerConfiguration configuration, File srcDir, String basePackage) {
		this.graph = graph;
		this.configuration = configuration;
		this.srcDir = srcDir;
		this.basePackage = basePackage;
		srcDir.mkdirs();
	}

	public void render() throws IntinoException {
		renderMessages();
		renderMeasurements();
		renderResources();
		renderDatamarts();
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

	private void renderDatamarts() throws IntinoException {
		new DatamartsRenderer(srcDir, graph, configuration, basePackage).render();
	}

	private List<Sensor> measurements() {
		return graph.datalake() == null ? Collections.emptyList() : graph.datalake().tankList().stream()
				.filter(Datalake.Tank::isMeasurement)
				.map(tank -> tank.asMeasurement().sensor())
				.collect(Collectors.toList());
	}
}