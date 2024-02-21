package io.intino.ness.datalakeinspector.box.regenerator;


import io.intino.alexandria.event.Event;
import io.intino.ness.datalakeinspector.box.pump.EventPump;

import java.util.function.Function;

public interface Mapper extends Function<Event, Event> {
	Event apply(Event event);

	Filter filter();

	String description();

	interface Filter extends EventPump.Reflow.Filter {
		boolean allow(Event event);
	}
}
