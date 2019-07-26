package io.intino.datahub.graph;

import io.intino.datahub.broker.BrokerService;

@FunctionalInterface
public interface BrokerImplementation {
	BrokerService get();
}
