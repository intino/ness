package io.intino.datahub.model.rules;

import io.intino.magritte.lang.model.Node;
import io.intino.magritte.lang.model.rules.NodeRule;

public class RequiresId implements NodeRule {


	public boolean accept(Node node) {
		return node.components().stream().anyMatch(c -> c.appliedAspects().stream().anyMatch(a -> a.type().equals("Id")));
	}

	@Override
	public String errorMessage() {
		return "This Concept requires an Id component";
	}
}
