package io.intino.datahub.model.rules;


import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.rules.NodeRule;

public class Named implements NodeRule {
	@Override
	public boolean accept(Mogram mogram) {
		return !mogram.isAnonymous();
	}

	@Override
	public String errorMessage() {
		return "This element must have name";
	}
}
