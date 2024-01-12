package io.intino.datahub.model;

import io.intino.magritte.framework.Graph;

public class NessGraph extends io.intino.datahub.model.AbstractGraph {

	public NessGraph(Graph graph) {
		super(graph);
	}

	public NessGraph(io.intino.magritte.framework.Graph graph, NessGraph wrapper) {
		super(graph, wrapper);
	}


	public static NessGraph load(io.intino.magritte.io.Stash... startingModel) {
		return new Graph().loadLanguage("Ness", _language()).loadStashes(startingModel).as(NessGraph.class);
	}

	public static NessGraph load(io.intino.magritte.framework.Store store, io.intino.magritte.io.Stash... startingModel) {
		return new Graph(store).loadLanguage("Ness", _language()).loadStashes(startingModel).as(NessGraph.class);
	}
}