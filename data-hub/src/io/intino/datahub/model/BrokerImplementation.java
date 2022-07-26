package io.intino.datahub.model;

import io.intino.datahub.broker.BrokerService;

@FunctionalInterface
public interface BrokerImplementation {
	BrokerService get();
}
