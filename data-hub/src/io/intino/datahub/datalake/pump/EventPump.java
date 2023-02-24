package io.intino.datahub.datalake.pump;

import io.intino.alexandria.Timetag;
import io.intino.alexandria.datalake.Datalake.Store.Source;
import io.intino.alexandria.datalake.Datalake.Store.Tank;
import io.intino.alexandria.event.Event;


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