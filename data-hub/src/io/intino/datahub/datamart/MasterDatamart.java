package io.intino.datahub.datamart;

import io.intino.alexandria.Timetag;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.Message;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.datamart.mounters.MasterDatamartMounter;
import io.intino.datahub.model.Datalake;
import io.intino.datahub.model.Datamart;
import io.intino.datahub.model.rules.SnapshotScale;
import io.intino.sumus.chronos.ReelFile;
import io.intino.sumus.chronos.TimelineStore;
import org.apache.commons.io.FileUtils;

import java.io.Closeable;
import java.io.File;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

public interface MasterDatamart extends Closeable {

	Datamart definition();

	DataHubBox box();

	String name();

	Store<Message> entityStore();

	ChronosDirectory<TimelineStore> timelineStore();

	ChronosDirectory<ReelFile> reelStore();

	Stream<MasterDatamartMounter> createMountersFor(Datalake.Tank tank);

	TimeShiftCache cacheOf(String timeline);

	default Instant ts() {
		return entityStore().stream().map(m -> m.get("ts").asInstant()).filter(Objects::nonNull).max(Comparator.naturalOrder()).orElse(null);
	}

	default void clear() {
		entityStore().clear();
		timelineStore().clear();
		reelStore().clear();
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

	abstract class ChronosDirectory<T> {

		private final File root;

		public ChronosDirectory(File root) {
			this.root = root;
		}

		protected abstract String extension();

		public int size() {
			return listFiles().size();
		}

		public abstract T get(String type, String id);

		public boolean contains(String type, String id) {
			return fileOf(type, id).exists();
		}

		public void remove(String type, String id) {
			fileOf(type, id).delete();
		}

		public void clear() {
			for (File file : listFiles()) {
				try {
					file.delete();
				} catch (Exception e) {
					// TODO: ??
					Logger.error(e);
				}
			}
		}

		public abstract Stream<T> stream();

		public abstract Collection<String> subscribedEvents();

		public abstract boolean isSubscribedTo(Datalake.Tank tank);

		protected File fileOf(String type, String id) {
			return new File(root, normalizePath(type + File.pathSeparator + id + extension()));
		}

		public static String normalizePath(String path) {
			return path.replace(":", "-");
		}

		protected List<File> listFiles() {
			return root.exists()
					? new ArrayList<>(FileUtils.listFiles(root, new String[]{extension(), extension().substring(1)}, true))
					: emptyList();
		}
	}

	record Snapshot(Timetag timetag, MasterDatamart datamart) {

		public static boolean shouldCreateSnapshot(Timetag oldTimetag, Timetag newTimetag, SnapshotScale scale, io.intino.datahub.model.rules.DayOfWeek firstDayOfWeek) {
			return switch (scale) { // TODO: check
				case None -> false;
				case Day -> ChronoUnit.DAYS.between(oldTimetag.date(), newTimetag.date()) >= 1;
				case Month -> ChronoUnit.MONTHS.between(oldTimetag.date(), newTimetag.date()) >= 1;
				case Year -> ChronoUnit.YEARS.between(oldTimetag.date(), newTimetag.date()) >= 1;
				case Week -> ChronoUnit.WEEKS.between(oldTimetag.date(), newTimetag.date()) >= 1;
			};
		}

		public static boolean shouldCreateSnapshot(Timetag timetag, SnapshotScale scale, io.intino.datahub.model.rules.DayOfWeek firstDayOfWeek) {
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

		private static boolean isFirstDayOfWeek(Timetag today, io.intino.datahub.model.rules.DayOfWeek firstDayOfWeek) {
			return today.datetime().getDayOfWeek().name().equalsIgnoreCase(firstDayOfWeek.name());
		}
	}
}
