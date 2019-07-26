package io.intino.ness.datahub.graph.functions;

import io.intino.ness.datahub.broker.BrokerService;

@FunctionalInterface
public interface BrokerImplementation {
	BrokerService get();
}
