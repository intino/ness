package io.intino.ness.triton.graph;

import io.intino.tara.magritte.Graph;

public class ServiceGraph extends io.intino.ness.triton.graph.AbstractGraph {

	public ServiceGraph(Graph graph) {
		super(graph);
	}

	public ServiceGraph(io.intino.tara.magritte.Graph graph, ServiceGraph wrapper) {
	    super(graph, wrapper);
	}
}