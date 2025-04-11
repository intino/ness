package systems.intino.eventsourcing.datahubbuilder.codegeneration.ontology;

import io.intino.builder.CompilerConfiguration;
import systems.intino.eventsourcing.datahub.model.Message;
import systems.intino.eventsourcing.datahub.model.NessGraph;
import systems.intino.eventsourcing.datahub.model.Resource;
import systems.intino.eventsourcing.datahubbuilder.IntinoException;
import systems.intino.eventsourcing.datahubbuilder.codegeneration.datamarts.DatamartsRenderer;
import systems.intino.eventsourcing.datahubbuilder.codegeneration.message.MessageRenderer;
import systems.intino.eventsourcing.datahubbuilder.codegeneration.resource.ResourceRenderer;

import java.io.File;

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
		this.srcDir.mkdirs();
	}

	public void render() throws IntinoException {
		renderMessages();
		renderResources();
		renderDatamarts();
	}

	private void renderMessages() {
		graph.core$().find(Message.class).forEach(event -> new MessageRenderer(event, srcDir, basePackage).render());
	}

	private void renderResources() {
		graph.core$().find(Resource.class).forEach(r -> new ResourceRenderer(r, srcDir, basePackage).render());
	}

	private void renderDatamarts() throws IntinoException {
		new DatamartsRenderer(srcDir, graph, configuration, basePackage).render();
	}
}