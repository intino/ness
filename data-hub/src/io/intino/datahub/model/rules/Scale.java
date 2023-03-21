package io.intino.datahub.model.rules;

import io.intino.magritte.lang.model.Rule;

import java.time.temporal.ChronoUnit;

public enum Scale implements Rule<Enum> {
	Year(ChronoUnit.YEARS),
	Month(ChronoUnit.MONTHS),
	Day(ChronoUnit.DAYS),
	Hour(ChronoUnit.HOURS),
	Minute(ChronoUnit.MINUTES),
	None(ChronoUnit.FOREVER);

	private final ChronoUnit chronoUnit;

	Scale(ChronoUnit chronoUnit) {
		this.chronoUnit = chronoUnit;
	}


	public ChronoUnit chronoUnit() {
		return chronoUnit;
	}

	@Override
	public boolean accept(Enum value) {
		return value instanceof Scale;
	}
}
