package io.intino.ness.konos.actions;

import io.intino.ness.DatalakeManager;
import io.intino.ness.konos.NessBox;

abstract class Action {

	protected static final String OK = ":ok_hand:";

	protected DatalakeManager datalake(NessBox box) {
		return box.get(DatalakeManager.class);
	}
}
