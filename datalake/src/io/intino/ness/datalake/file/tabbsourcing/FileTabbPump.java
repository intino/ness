package io.intino.ness.datalake.file.tabbsourcing;

import io.intino.ness.datalake.Datalake;
import io.intino.ness.datalake.file.eventsourcing.EventHandler;
import io.intino.ness.datalake.file.eventsourcing.EventPump;

public class FileTabbPump implements TabbPump {
	private final Datalake.SetStore store;

	public FileTabbPump(Datalake.SetStore store) {
		this.store = store;
	}


	@Override
	public EventPump.Reflow reflow(Reflow.Filter filter) {
		return new EventPump.Reflow() {
			@Override
			public void next(int blockSize, EventHandler... eventHandlers) {

			}

			@Override
			public boolean hasNext() {
				return false;
			}
		};
	}
}
