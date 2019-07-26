package io.intino.ness.datahub.graph.rules;

import io.intino.tara.lang.model.Node;
import io.intino.tara.lang.model.rules.NodeRule;

public class FacetRequired implements NodeRule {
	public boolean accept(Node node) {
		return !node.facets().isEmpty();
	}


	@Override
	public String errorMessage() {
		return "Tanks must be of a type. Add corresponding facet";
	}
}
