package io.intino.datahub.model.rules;


import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.rules.NodeRule;

public class OnlyNumeric implements NodeRule {


	public boolean accept(Mogram mogram) {
		return mogram.appliedFacets().stream().noneMatch(a -> a.type().contains("Text") || a.type().contains("Table") || a.type().equals("Word"));
	}

	@Override
	public String errorMessage() {
		return "Accepted Aspects: Real, Integer, LongInteger, Bool, Date or DateTime";
	}
}
