package io.intino.datahub.datalake.regenerator;


import io.intino.alexandria.datalake.file.eventsourcing.EventPump;
import io.intino.alexandria.event.Event;

import java.util.function.Function;

public interface Mapper extends Function<Event, Event> {
	Event apply(Event event);

	Filter filter();

	String description();

	interface Filter extends EventPump.Reflow.Filter {
		boolean allow(Event event);
	}
}
