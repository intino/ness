package io.intino.datahub.datamart.mounters;

import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.message.Message;
import io.intino.datahub.datamart.MasterDatamart;
import io.intino.sumus.chronos.TimelineFile;

import java.io.File;
import java.io.IOException;

import static io.intino.datahub.box.DataHubBox.TIMELINE_EXTENSION;

public final class TimelineMounter extends MasterDatamartMounter {

	public TimelineMounter(MasterDatamart datamart) {
		super(datamart);
	}

	@Override
	public void mount(Message message) {
		if(message == null) return;
		try {
			String ss = message.get("ss").asString();
			if(ss == null) return;

			TimelineFile timelineFile = datamart.timelineStore().get(ss);
			if(timelineFile == null) timelineFile = createTimelineFile(ss);
			update(timelineFile, message);

		} catch (Exception e) {
			Logger.error("Could not mount message " + message + ": " + e.getMessage(), e);
		}
	}

	private void update(TimelineFile timelineFile, Message message) {
		// TODO
	}

	private TimelineFile createTimelineFile(String ss) throws IOException {
		return TimelineFile.create(new File(box().datamartTimelinesDirectory(datamart.name()), ss + TIMELINE_EXTENSION), ss);
	}
}
