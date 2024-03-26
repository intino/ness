package io.intino.datahub.model.rules;

import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.rules.NodeRule;

public class RequiresId implements NodeRule {


	public boolean accept(Mogram mogram) {
		return mogram.components().stream().anyMatch(c -> c.appliedFacets().stream().anyMatch(a -> a.type().equals("Id")));
	}

	@Override
	public String errorMessage() {
		return "This Concept requires an Id component";
	}
}
