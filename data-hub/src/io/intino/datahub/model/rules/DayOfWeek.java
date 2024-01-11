package io.intino.datahub.model.rules;


import io.intino.tara.language.model.Rule;

public enum DayOfWeek implements Rule<Enum> {

	MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;

	@Override
	public boolean accept(Enum value) {
		return value instanceof DayOfWeek;
	}
}
