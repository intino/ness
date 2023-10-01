package io.intino.datahub.datamart.mounters;

import io.intino.alexandria.event.Event;
import io.intino.alexandria.event.measurement.MeasurementEvent;
import io.intino.alexandria.event.message.MessageEvent;
import io.intino.alexandria.message.Message;
import io.intino.datahub.datamart.MasterDatamart;
import io.intino.datahub.model.Timeline;

public final class TimelineMounter extends MasterDatamartMounter {

	private final TimelineRawMounter fromMeasurementMounter;
	private final TimelineAssertionMounter assertionMounter;
	private final TimelineCookedMounter summaryMounter;

	public TimelineMounter(MasterDatamart datamart) {
		super(datamart);
		fromMeasurementMounter = new TimelineRawMounter(box(), datamart);
		assertionMounter = new TimelineAssertionMounter(box(), datamart);
		summaryMounter = new TimelineCookedMounter(box(), datamart);
	}

	@Override
	public void mount(Event event) {
		synchronized (datamart) {
			if (event instanceof MeasurementEvent e) fromMeasurementMounter.mount(e);
			if (event instanceof MessageEvent e) mount(e.toMessage());
		}
	}

	@Override
	public void mount(Message message) {
		if (message == null) return;
		if (isAssertion(message)) assertionMounter.mount(new MessageEvent(message));
		if (isCooked(message)) summaryMounter.mount(new MessageEvent(message));
		else fromMeasurementMounter.mount((measurementEvent(message)));
	}

	private boolean isAssertion(Message message) {
		return datamart.definition().timelineList().stream()
				.filter(t -> t.entity().from() != null)
				.anyMatch(t -> t.entity().from().message().name$().equals(message.type()));
	}

	private boolean isCooked(Message message) {
		return datamart.definition().timelineList().stream()
				.filter(Timeline::isCooked)
				.anyMatch(t -> t.asCooked().timeSeriesList().stream().anyMatch(ts -> ts.tank().message().name$().equals(message.type())));
	}

	private static MeasurementEvent measurementEvent(Message message) {
		return new MeasurementEvent(message.type(), message.get("ss").asString(), message.get("ts").asInstant(), message.get("measurements").as(String[].class), java.util.Arrays.stream(message.get("values").as(String[].class)).mapToDouble(Double::parseDouble).toArray());
	}
}
