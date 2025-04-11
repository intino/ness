package io.intino.ness.datalakeinspector.box.regenerator;


import io.intino.ness.datalakeinspector.box.pump.EventPump;
import systems.intino.eventsourcing.event.Event;

import java.util.function.Function;

public interface Mapper extends Function<Event, Event> {
	Event apply(Event event);

	Filter filter();

	String description();

	interface Filter extends EventPump.Reflow.Filter {
		boolean allow(Event event);
	}
}
