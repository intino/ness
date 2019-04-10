package io.intino.ness.datalake.file.tabbsourcing;

import io.intino.ness.datalake.Datalake;
import io.intino.ness.datalake.file.eventsourcing.EventPump;

public class FileTabbPump implements TabbPump {
	private final Datalake.SetStore store;

	public FileTabbPump(Datalake.SetStore store) {
		this.store = store;
	}


	@Override
	public EventPump.Reflow reflow(Reflow.Filter filter) {
		return (blockSize, populationHandlers) -> {

		};
	}
}
