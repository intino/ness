package io.intino.ness.datalake.dataframesourcing;


import io.intino.ness.datalake.eventsourcing.EventPump;

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
