package io.intino.ness.triton.graph;

import io.intino.tara.magritte.Graph;

public class ServiceJmsGraph extends AbstractGraph {

	public ServiceJmsGraph(Graph graph) {
		super(graph);
	}

	public ServiceJmsGraph(io.intino.tara.magritte.Graph graph, ServiceJmsGraph wrapper) {
	    super(graph, wrapper);
	}
}