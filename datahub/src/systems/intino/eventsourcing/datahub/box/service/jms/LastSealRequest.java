package systems.intino.eventsourcing.datahub.box.service.jms;

import systems.intino.eventsourcing.datahub.box.DatahubBox;
import systems.intino.eventsourcing.event.EventHub.RequestConsumer;

public class LastSealRequest implements RequestConsumer {
	private final DatahubBox box;

	public LastSealRequest(DatahubBox box) {
		this.box = box;
	}

	public String accept(String request) {
		try {
			return box.lastSeal().toString();
		} catch (Throwable e) {
			io.intino.alexandria.logger.Logger.error(e.getMessage(), e);
			return null;
		}
	}
}