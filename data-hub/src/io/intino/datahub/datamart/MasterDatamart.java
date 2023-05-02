package io.intino.datahub.datamart;

import io.intino.alexandria.Timetag;
import io.intino.alexandria.message.Message;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.datamart.mounters.MasterDatamartMounter;
import io.intino.datahub.model.Datalake;
import io.intino.datahub.model.Datamart;
import io.intino.datahub.model.rules.SnapshotScale;
import io.intino.sumus.chronos.ReelFile;
import io.intino.sumus.chronos.TimelineFile;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

public interface MasterDatamart {

	Datamart definition();

	DataHubBox box();

	String name();

	Store<Message> entityStore();

	Store<TimelineFile> timelineStore();

	Store<ReelFile> reelStore();

	Stream<MasterDatamartMounter> createMountersFor(Datalake.Tank tank);

	interface Store<T> {
		int size();
		boolean contains(String id);
		T get(String id);
		void put(String id, T value);
		void remove(String id);
		Stream<T> stream();
		Map<String, T> toMap();
		Collection<String> subscribedEvents();
		boolean isSubscribedTo(Datalake.Tank tank);
	}

	record Snapshot(Timetag timetag, MasterDatamart datamart) {

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
