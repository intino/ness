package io.intino.ness.datahubterminalplugin.datamarts;

import java.util.List;

public interface Parameter {

	static Parameter of(List<?> values) {
		return () -> values;
	}

	List<?> values();
}
