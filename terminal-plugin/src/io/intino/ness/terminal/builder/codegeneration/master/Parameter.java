package io.intino.ness.terminal.builder.codegeneration.master;

import java.util.List;

public interface Parameter {

	static Parameter of(List<?> values) {
		return () -> values;
	}

	List<?> values();
}
