package io.intino.datahub.model.rules;

import io.intino.magritte.lang.model.Node;
import io.intino.magritte.lang.model.rules.NodeRule;

public class OnlyNumeric implements NodeRule {


	public boolean accept(Node node) {
		return node.appliedAspects().stream().noneMatch(a -> a.type().contains("Text") || a.type().contains("Table") || a.type().equals("Word"));
	}

	@Override
	public String errorMessage() {
		return "Accepted Aspects: Real, Integer, LongInteger, Bool, Date or DateTime";
	}
}
