package io.intino.ness.datalake.file.dataframesourcing;


import io.intino.ness.datalake.file.eventsourcing.EventPump;

public interface DataframePump {

	EventPump.Reflow reflow(Reflow.Filter filter);

	interface Reflow {
		void next(int blockSize, PopulationHandler... eventHandlers);

		public interface Filter {
		}
	}

	interface PopulationHandler {

	}

}
