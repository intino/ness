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

	class Snapshot<T> {

		private final Timetag timetag;
		private final MasterDatamart<T> datamart;

		public Snapshot(Timetag timetag, MasterDatamart<T> datamart) {
			this.timetag = timetag;
			this.datamart = datamart;
		}

		public Timetag timetag() {
			return timetag;
		}

		public MasterDatamart<T> datamart() {
			return datamart;
		}

		public static boolean shouldCreateSnapshot(Timetag timetag, SnapshotScale scale) {
			switch(scale) {
				case Year: return isFirstDayOfYear(timetag);
				case Month: return isFirstDayOfMonth(timetag);
				case Week: return isFirstDayOfWeek(timetag);
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

		private static boolean isFirstDayOfWeek(Timetag today) {
			return today.datetime().getDayOfWeek() == DayOfWeek.MONDAY; // TODO: specify first day of week?
		}
	}
}
