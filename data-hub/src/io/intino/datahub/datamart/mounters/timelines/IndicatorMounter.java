package io.intino.datahub.datamart.mounters.timelines;

import io.intino.alexandria.logger.Logger;
import io.intino.datahub.datamart.MasterDatamart;
import io.intino.datahub.datamart.MasterDatamart.IndicatorDirectory;
import io.intino.datahub.model.Indicator;
import io.intino.datahub.model.IndicatorFile;
import io.intino.sumus.chronos.Magnitude;
import io.intino.sumus.chronos.Timeline;
import io.intino.sumus.chronos.TimelineStore;

public class IndicatorMounter {
	private final MasterDatamart datamart;

	public IndicatorMounter(MasterDatamart datamart) {
		this.datamart = datamart;
	}

	public void mount(String timeline, TimelineStore timelineStore) {
		if (timelineStore == null) return;
		IndicatorDirectory indicatorStore = datamart.indicatorStore();
		try {
			Timeline.Point last = timelineStore.timeline().last();
			if (last == null || last.instant() == null) return;
			for (Magnitude magnitude : timelineStore.sensorModel().magnitudes()) {
				IndicatorFile indicatorFile = indicatorStore.get(timeline + "." + magnitude.label());
				Indicator indicator = indicatorFile.get();
				indicator.put(timelineStore.sensor(), last.instant(), last.value(magnitude));
				indicatorFile.save(indicator);
			}
		} catch (Exception e) {
			Logger.error(e);
		}
	}
}
