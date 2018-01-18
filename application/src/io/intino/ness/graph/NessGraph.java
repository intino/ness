package io.intino.ness.graph;

import io.intino.tara.magritte.Graph;

public class NessGraph extends io.intino.ness.graph.AbstractGraph {

	public NessGraph(Graph graph) {
		super(graph);
	}


	public Tank tank(String name) {
		return tankList(t -> t.qualifiedName().equalsIgnoreCase(name)).findFirst().orElse(null);
	}
}