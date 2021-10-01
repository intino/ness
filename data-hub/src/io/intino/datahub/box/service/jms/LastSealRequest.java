package io.intino.datahub.box.service.jms;

import io.intino.alexandria.event.EventHub.RequestConsumer;
import io.intino.datahub.box.DataHubBox;

public class LastSealRequest implements RequestConsumer {
	private final DataHubBox box;

	public LastSealRequest(DataHubBox box) {
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