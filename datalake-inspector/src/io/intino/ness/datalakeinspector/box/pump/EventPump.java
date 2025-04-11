package io.intino.ness.datalakeinspector.box.pump;

import io.intino.alexandria.Timetag;
import systems.intino.eventsourcing.datalake.Datalake.Store.Source;
import systems.intino.eventsourcing.datalake.Datalake.Store.Tank;
import systems.intino.eventsourcing.event.Event;


public interface EventPump {
	Reflow reflow(Reflow.Filter filter);

	interface Reflow {
		void next(int blockSize, EventHandler... eventHandlers);

		boolean hasNext();

		interface Filter {
			boolean allow(Tank<? extends Event> tank);

			boolean allow(Tank<? extends Event> tank, Source<? extends Event> source, Timetag timetag);
		}
	}

	interface ReflowHandler {
		void onBlock(int reflowedMessages);

		void onFinish(int reflowedMessages);
	}


	interface EventHandler {
		void handle(Event event);
	}
}