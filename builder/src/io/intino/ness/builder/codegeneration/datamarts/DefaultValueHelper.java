package io.intino.ness.builder.codegeneration.datamarts;

import io.intino.magritte.framework.Node;

import java.util.List;

public class DefaultValueHelper {

	public static Parameter getDefaultValue(Node node) {
		Parameter defaultValue = parameter(node, "defaultValue");
		if(defaultValue == null) return null;
		if(defaultValue.values() == null || defaultValue.values().isEmpty()) return null;
		return defaultValue.values().get(0) != null ? defaultValue : null;
	}

	private static Parameter parameter(Node c, String name) {
		List<?> values = c.variables().get(name);
		return values == null ? null : Parameter.of(values);
	}
}
