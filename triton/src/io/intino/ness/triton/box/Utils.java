package io.intino.ness.triton.box;

import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.ness.datalake.Datalake.EventStore.Tank;
import io.intino.ness.triton.graph.Datalake;
import io.intino.ness.triton.graph.TritonGraph;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import static io.intino.alexandria.Timetag.of;
import static java.time.LocalDateTime.ofInstant;
import static java.time.ZoneOffset.UTC;

public class Utils {
	public static Tank findTank(io.intino.ness.datalake.Datalake datalake, String name) {
		return datalake.eventStore().tank(name);
	}

	public static Datalake.Tank findTank(TritonGraph graph, String name) {
		final List<Datalake.Tank> tanks = graph.datalake().tankList(t -> t.name().equalsIgnoreCase(name));
		return tanks.isEmpty() ? null : tanks.get(0);
	}

	public static Stream<Tank> sortedTanks(io.intino.ness.datalake.Datalake datalake) {
		return datalake.eventStore().tanks().sorted((s1, s2) -> String.CASE_INSENSITIVE_ORDER.compare(s1.name(), s2.name()));
	}

	public static Timetag timetag(Instant instant, Scale scale) {
		return of(ofInstant(instant, UTC), scale);
	}

}
