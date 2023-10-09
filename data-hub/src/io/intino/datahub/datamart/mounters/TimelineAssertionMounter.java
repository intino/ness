package io.intino.datahub.datamart.mounters;

import io.intino.alexandria.event.message.MessageEvent;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.datamart.MasterDatamart;
import io.intino.datahub.model.Timeline;
import io.intino.sumus.chronos.TimelineStore;
import io.intino.sumus.chronos.timelines.TimelineWriter;

import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;

import static io.intino.datahub.box.DataHubBox.TIMELINE_EXTENSION;
import static io.intino.datahub.datamart.mounters.TimelineUtils.sensorModel;

public class TimelineAssertionMounter {
	private final DataHubBox box;
	private final MasterDatamart datamart;

	public TimelineAssertionMounter(DataHubBox box, MasterDatamart datamart) {
		this.box = box;
		this.datamart = datamart;
	}

	void mount(MessageEvent assertion) {
		datamart.definition().timelineList().stream()
				.filter(t -> t.entity().from() != null && t.entity().from().message().name$().equals(assertion.type()))
				.findFirst()
				.ifPresent(t -> updateSensorModel(assertion, t));
	}

	void updateSensorModel(MessageEvent assertion, Timeline t) {
		try {
			File timelineDirectory = new File(box.datamartTimelinesDirectory(datamart.name()), t.asRaw().tank().sensor().name$());
			File file = new File(timelineDirectory, assertion.toMessage().get("id").asString() + TIMELINE_EXTENSION);
			if (!file.exists()) return;
			try(TimelineWriter writer = TimelineStore.of(file).writer()) {
				writer.sensorModel(sensorModel(TimelineStore.of(file).sensorModel(), assertion.toMessage(), t));
			}
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public static class OfSingleTimeline extends TimelineAssertionMounter {

		private final Timeline timeline;
		private final Supplier<TimelineWriter> writer;

		public OfSingleTimeline(MasterDatamart datamart, Timeline timeline, Supplier<TimelineWriter> writer) {
			super(datamart.box(), datamart);
			this.timeline = timeline;
			this.writer = writer;
		}

		@Override
		void mount(MessageEvent assertion) {
			try {
				writer.get().sensorModel(sensorModel(writer.get().sensorModel(), assertion.toMessage(), timeline));
			} catch (IOException e) {
				Logger.error(e);
			}
		}
	}
}
