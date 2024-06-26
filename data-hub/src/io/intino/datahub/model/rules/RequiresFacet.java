package io.intino.datahub.model.rules;

import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.rules.MogramRule;

public class RequiresFacet implements MogramRule {
	public boolean accept(Mogram node) {
		return !node.appliedFacets().isEmpty();
	}


	@Override
	public String errorMessage() {
		return "This parameters should have a type as facet";
	}
}
