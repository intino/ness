package io.intino.master.model.rules;

import io.intino.magritte.lang.model.Node;
import io.intino.magritte.lang.model.rules.NodeRule;

public class RequiresAspect implements NodeRule {
	public boolean accept(Node node) {
		return !node.appliedAspects().isEmpty();
	}

	@Override
	public String errorMessage() {
		return "This concept must have declared with an aspect";
	}
}
