package io.intino.datahub.model.rules;

import io.intino.tara.language.model.Rule;

public enum Scale implements Rule<Enum> {

	Year, Month, Day, Hour, Minute;

	@Override
	public boolean accept(Enum value) {
		return value instanceof Scale;
	}
}
