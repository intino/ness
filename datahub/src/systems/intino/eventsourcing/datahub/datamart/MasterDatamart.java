package systems.intino.eventsourcing.datahub.datamart;

import io.intino.alexandria.Timetag;
import systems.intino.eventsourcing.datahub.box.DatahubBox;
import systems.intino.eventsourcing.datahub.datamart.impl.SubjectsStore;
import systems.intino.eventsourcing.datahub.datamart.mounters.DatamartMounter;
import systems.intino.eventsourcing.datahub.model.Datalake;
import systems.intino.eventsourcing.datahub.model.Datamart;
import systems.intino.eventsourcing.datahub.model.rules.DayOfWeek;
import systems.intino.eventsourcing.datahub.model.rules.SnapshotScale;

import java.io.Closeable;
import java.io.File;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface MasterDatamart extends Closeable {

	Datamart definition();

	DatahubBox box();

	String name();

	SubjectsStore subjectsStore();

	List<? extends DatamartMounter> mountersFor(Datalake.Tank tank);

	default void clear() {
		subjectsStore().clear();
	}

	void close();

	interface Store<T> {
		int size();

		boolean contains(String id);

		T get(String id);

		void put(String id, T value);

		void remove(String id);

		void clear();

		Stream<T> stream();

		Map<String, T> toMap();

		Collection<String> subscribedEvents();

		boolean isSubscribedTo(Datalake.Tank tank);
	}

	static String normalizePath(String path) {
		return path.replace(":", "-");
	}


	record Snapshot(Timetag timetag, MasterDatamart datamart) {

		public static boolean shouldCreateSnapshot(Timetag oldTimetag, Timetag newTimetag, SnapshotScale scale, DayOfWeek firstDayOfWeek) {
			return switch (scale) { // TODO: check
				case None -> false;
				case Day -> ChronoUnit.DAYS.between(oldTimetag.date(), newTimetag.date()) >= 1;
				case Month -> ChronoUnit.MONTHS.between(oldTimetag.date(), newTimetag.date()) >= 1;
				case Year -> ChronoUnit.YEARS.between(oldTimetag.date(), newTimetag.date()) >= 1;
				case Week -> ChronoUnit.WEEKS.between(oldTimetag.date(), newTimetag.date()) >= 1;
			};
		}

		public static boolean shouldCreateSnapshot(Timetag timetag, SnapshotScale scale, DayOfWeek firstDayOfWeek) {
			return switch (scale) {
				case None -> false;
				case Day -> true;
				case Year -> isFirstDayOfYear(timetag);
				case Month -> isFirstDayOfMonth(timetag);
				case Week -> isFirstDayOfWeek(timetag, firstDayOfWeek);
			};
		}

		private static boolean isFirstDayOfYear(Timetag today) {
			return today.month() == 1 && today.day() == 1;
		}

		private static boolean isFirstDayOfMonth(Timetag today) {
			return today.day() == 1;
		}

		private static boolean isFirstDayOfWeek(Timetag today, DayOfWeek firstDayOfWeek) {
			return today.datetime().getDayOfWeek().name().equalsIgnoreCase(firstDayOfWeek.name());
		}
	}
}
