package systems.intino.eventsourcing.datahub.model;

import systems.intino.eventsourcing.datahub.broker.BrokerService;

@FunctionalInterface
public interface BrokerImplementation {
	BrokerService get();
}
