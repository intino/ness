package io.intino.datahub.model.rules;


import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.MogramRoot;
import io.intino.tara.language.model.rules.NodeRule;

import java.util.List;

public class AvoidEventAttributes implements NodeRule {
	private List<Mogram> found;

	public boolean accept(Mogram mogram) {
		if (!(mogram.container() instanceof MogramRoot) && !mogram.container().metaTypes().contains("Namespace"))
			return true;
		return ((found = mogram.component("ts")).isEmpty()) &&
				(found = mogram.component("ss")).isEmpty() &&
				(found = mogram.component("type")).isEmpty();
	}

	@Override
	public String errorMessage() {
		return "Message cannot have 'ts', 'ss' or 'type' attributes. Found: " + found.get(0).name();
	}
}
