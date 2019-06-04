package io.intino.ness.triton.datalake;

import io.intino.ness.datalake.Datalake.EventStore.Tank;

public class Probes {
	public static String feed(Tank tank) {
		return "feed." + tank.name();
	}

	public static String flow(Tank tank) {
		return "flow." + tank.name();
	}

	public static String put(Tank tank) {
		return "put." + tank.name();
	}

	public static String feed(io.intino.ness.triton.graph.Tank tank) {
		return "feed." + tank.name();
	}

	public static String flow(io.intino.ness.triton.graph.Tank tank) {
		return "flow." + tank.name();
	}

	public static String put(io.intino.ness.triton.graph.Tank tank) {
		return "put." + tank.name();
	}
}
