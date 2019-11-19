package io.intino.datahub.graph.rules;

import io.intino.tara.lang.model.Node;
import io.intino.tara.lang.model.rules.NodeRule;

public class AspectRequired implements NodeRule {
	public boolean accept(Node node) {
		return !node.appliedAspects().isEmpty();
	}

	@Override
	public String errorMessage() {
		return "Tanks must be of a type. Add corresponding facet";
	}
}