package io.intino.datahub.graph;

import io.intino.tara.magritte.Graph;

public class NessGraph extends AbstractGraph {

	public NessGraph(Graph graph) {
		super(graph);
	}

	public NessGraph(io.intino.tara.magritte.Graph graph, NessGraph wrapper) {
		super(graph, wrapper);
	}
}