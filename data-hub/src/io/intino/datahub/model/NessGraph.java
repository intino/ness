package io.intino.datahub.model;

import io.intino.magritte.framework.Graph;

public class NessGraph extends AbstractGraph {

	public NessGraph(Graph graph) {
		super(graph);
	}

	public NessGraph(io.intino.magritte.framework.Graph graph, NessGraph wrapper) {
		super(graph, wrapper);
	}
}