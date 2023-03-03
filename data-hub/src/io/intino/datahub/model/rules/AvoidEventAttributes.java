package io.intino.datahub.model.rules;

import io.intino.magritte.lang.model.Node;
import io.intino.magritte.lang.model.NodeRoot;
import io.intino.magritte.lang.model.rules.NodeRule;

import java.util.List;

public class AvoidEventAttributes implements NodeRule {
	private List<Node> found;

	public boolean accept(Node node) {
		if (!(node.container() instanceof NodeRoot) && !node.container().metaTypes().contains("Namespace")) return true;
		return ((found = node.component("ts")).isEmpty()) &&
				(found = node.component("ss")).isEmpty() &&
				(found = node.component("type")).isEmpty();
	}

	@Override
	public String errorMessage() {
		return "Message cannot have 'ts', 'ss' or 'type' attributes. Found: " + found.get(0).name();
	}
}
