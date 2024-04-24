package io.intino.datahub.model.rules;


import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.rules.MogramRule;

public class RequiresId implements MogramRule {


	public boolean accept(Mogram node) {
		return node.components().stream().anyMatch(c -> c.appliedFacets().stream().anyMatch(a -> a.type().equals("Id")));
	}

	@Override
	public String errorMessage() {
		return "This concept requires an Id component";
	}
}
