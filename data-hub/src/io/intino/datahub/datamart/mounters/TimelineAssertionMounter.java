package io.intino.datahub.datamart.mounters;

import io.intino.alexandria.event.message.MessageEvent;
import io.intino.alexandria.logger.Logger;
import io.intino.datahub.box.DataHubBox;
import io.intino.datahub.datamart.MasterDatamart;
import io.intino.datahub.model.Timeline;
import io.intino.sumus.chronos.TimelineFile;

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
			File tlFile = new File(timelineDirectory, assertion.toMessage().get("id").asString() + TIMELINE_EXTENSION);
			if (!tlFile.exists()) return;
			TimelineFile timelineFile = TimelineFile.open(tlFile);
			timelineFile.sensorModel(sensorModel(timelineFile.sensorModel(), assertion.toMessage(), t));
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public static class OfSingleTimeline extends TimelineAssertionMounter {

		private final Supplier<TimelineFile> tlFile;

		public OfSingleTimeline(DataHubBox box, MasterDatamart datamart, Supplier<TimelineFile> tlFile) {
			super(box, datamart);
			this.tlFile = tlFile;
		}

		@Override
		void updateSensorModel(MessageEvent assertion, Timeline t) {
			try {
				TimelineFile timelineFile = tlFile.get();
				timelineFile.sensorModel(sensorModel(timelineFile.sensorModel(), assertion.toMessage(), t));
			} catch (IOException e) {
				Logger.error(e);
			}
		}
	}
}
