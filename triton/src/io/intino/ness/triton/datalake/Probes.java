package io.intino.ness.triton.datalake;

import io.intino.ness.datalake.Datalake.EventStore.Tank;
import io.intino.ness.triton.graph.Datalake;

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

	public static String feed(Datalake.Tank tank) {
		return "feed." + tank.name();
	}

	public static String flow(Datalake.Tank tank) {
		return "flow." + tank.name();
	}

	public static String put(Datalake.Tank tank) {
		return "put." + tank.name();
	}
}
