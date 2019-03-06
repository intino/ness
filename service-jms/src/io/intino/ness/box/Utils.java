package io.intino.ness.box;

import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.ness.core.Datalake;
import io.intino.ness.core.Datalake.EventStore.Tank;
import io.intino.ness.graph.NessGraph;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.intino.alexandria.Timetag.of;
import static java.time.LocalDateTime.ofInstant;
import static java.time.ZoneOffset.UTC;

public class Utils {
	public static Tank findTank(Datalake store, String name) {
		return store.eventStore().tank(name);
	}

	public static io.intino.ness.graph.Tank findTank(NessGraph graph, String name) {
		final List<io.intino.ness.graph.Tank> tanks = graph.tankList(t -> t.name().equalsIgnoreCase(name)).collect(Collectors.toList());
		return tanks.isEmpty() ? null : tanks.get(0);
	}

	public static Stream<Tank> sortedTanks(Datalake datalake) {
		return datalake.eventStore().tanks().sorted((s1, s2) -> String.CASE_INSENSITIVE_ORDER.compare(s1.name(), s2.name()));
	}

	public static Timetag timetag(Instant instant, Scale scale) {
		return of(ofInstant(instant, UTC), scale);
	}

}
