package io.intino.datahub.model.rules;

import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.rules.NodeRule;

public class FacetRequired implements NodeRule {
	public boolean accept(Mogram node) {
		return !node.appliedFacets().isEmpty();
	}

	@Override
	public String errorMessage() {
		return "Tanks must be of a type. Add corresponding facet";
	}
}
