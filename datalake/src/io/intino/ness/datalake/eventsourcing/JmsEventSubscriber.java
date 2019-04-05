package io.intino.ness.datalake.eventsourcing;

import io.intino.ness.datalake.Datalake.EventStore.Tank;

public class JmsEventSubscriber implements EventSubscriber {
	@Override
	public Connection connection() {
		return null;
	}


	@Override
	public Subscription subscribe(Tank tank) {
		return (clientId, messageHandlers) -> {

		};
	}

	@Override
	public void unsubscribe(Tank tank) {

	}

}
