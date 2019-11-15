package io.intino.datahub.graph;

import io.intino.tara.magritte.Node;


public class Model {

	public static String schemaName(Data.Object self) {
		final Schema schema = self.schema();
		StringBuilder fullName = new StringBuilder();
		Node node = schema.core$();
		while (node.is(Schema.class)) {
			fullName.insert(0, firstUpperCase(node.name()) + ".");
			node = node.owner();
		}
		return fullName.substring(0, fullName.length() - 1);
	}

	private static String firstUpperCase(String value) {
		return value.substring(0, 1).toUpperCase() + value.substring(1);
	}

}
