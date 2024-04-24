package io.intino.datahub.model.rules;

import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.rules.MogramRule;

public class TankTypeRequired implements MogramRule {
	public boolean accept(Mogram mogram) {
		return !mogram.appliedFacets().isEmpty();
	}

	@Override
	public String errorMessage() {
		return "Tanks must be of a type. Add corresponding facet";
	}
}
