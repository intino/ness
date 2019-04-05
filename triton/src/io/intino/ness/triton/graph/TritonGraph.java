package io.intino.ness.triton.graph;

import io.intino.tara.magritte.Graph;

public class TritonGraph extends AbstractGraph {

	public TritonGraph(Graph graph) {
		super(graph);
	}

	public TritonGraph(io.intino.tara.magritte.Graph graph, TritonGraph wrapper) {
		super(graph, wrapper);
	}
}