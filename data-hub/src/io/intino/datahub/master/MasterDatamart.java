package io.intino.datahub.master;

import io.intino.alexandria.Timetag;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.model.rules.SnapshotScale;

import java.time.DayOfWeek;
import java.util.Map;

public interface MasterDatamart<T> {

	//TODO: el historico (en cliente) se hace como un stream de (instant, entity), en el que cada entrada
	// representara un instante en el que se modifico esa entidad, y la entidad en ese momento
	// Esto require un reflow completo
	// Tambien se podria pedir un historico en un punto determinado del tiempo
	// en este ultimo, se puede tirar de snapshot tambien

	String name();

	int size();

	boolean contains(String id);

	T get(String id);

	void put(String id, T value);

	void putAll(MasterDatamart<T> other);

	void remove(String id);

	void clear();

	Map<String, T> toMap();

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