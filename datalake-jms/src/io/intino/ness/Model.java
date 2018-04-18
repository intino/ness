package io.intino.ness;

import io.intino.ness.datalake.graph.Tank;

public class Model {
	public static int version(Tank self) {
		try {
			String[] names = self.qualifiedName().split("\\.");
			return Integer.parseInt(names[names.length - 1]);
		} catch (NumberFormatException e) {
			return 0;
		}
	}
}
