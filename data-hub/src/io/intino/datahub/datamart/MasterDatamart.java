package io.intino.datahub.datamart;

import io.intino.alexandria.Timetag;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.model.rules.SnapshotScale;

import java.time.DayOfWeek;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

public interface MasterDatamart<T> {

	String name();

	int size();

	boolean contains(String id);

	T get(String id);

	void put(String id, T value);

	void putAll(MasterDatamart<T> other);

	void remove(String id);

	void clear();

	Stream<T> elements();

	Map<String, T> toMap();

	Class<T> elementType();

	Collection<String> subscribedEvents();

	record Snapshot<T>(Timetag timetag, MasterDatamart<T> datamart) {

		public static boolean shouldCreateSnapshot(Timetag timetag, SnapshotScale scale, io.intino.datahub.model.rules.DayOfWeek firstDayOfWeek) {
			switch (scale) {
				case None: return false;
				case Year: return isFirstDayOfYear(timetag);
				case Month: return isFirstDayOfMonth(timetag);
				case Week: return isFirstDayOfWeek(timetag, firstDayOfWeek);
				case Day: return true;
			}
			Logger.error("Unknown snapshot scale for datamarts: " + scale);
			return false;
		}

		private static boolean isFirstDayOfYear(Timetag today) {
			return today.month() == 1 && today.day() == 1;
		}

		private static boolean isFirstDayOfMonth(Timetag today) {
			return today.day() == 1;
		}

		private static boolean isFirstDayOfWeek(Timetag today, io.intino.datahub.model.rules.DayOfWeek firstDayOfWeek) {
			return today.datetime().getDayOfWeek().name().equalsIgnoreCase(firstDayOfWeek.name()); // TODO: specify first day of week?
		}
	}
}
