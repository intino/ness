package io.intino.datahub.model.rules;

import io.intino.magritte.lang.model.Rule;

public enum SnapshotScale implements Rule<Enum> {

	Year, Month, Week, Day;

	@Override
	public boolean accept(Enum value) {
		return value instanceof SnapshotScale;
	}
}
