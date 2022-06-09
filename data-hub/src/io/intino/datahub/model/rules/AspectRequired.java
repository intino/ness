package io.intino.datahub.model.rules;

import io.intino.magritte.lang.model.Node;
import io.intino.magritte.lang.model.rules.NodeRule;

public class AspectRequired implements NodeRule {
	public boolean accept(Node node) {
		return !node.appliedAspects().isEmpty();
	}

	@Override
	public String errorMessage() {
		return "Tanks must be of a type. Add corresponding facet";
	}
}
